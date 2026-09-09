plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

apply(from = "gradle/verify-source-layout.gradle.kts")

tasks.register("verifyArchitecture") {
    dependsOn("verifyKotlinSourceLayout")
    group = "verification"
    description = "Enforce dependency direction, engine encapsulation, and service boundaries."
    val rules = listOf(
        fileTree("domain/src") { include("**/commonMain/**/*.kt") } to listOf(
            "android.", "androidx.", "java.", "platform.", "doom.engine",
            "com.kunal26das.doom.data", "com.kunal26das.doom.presentation",
            "io.github.kunal26das.startup",
        ),
        fileTree("engine/src") { include("**/commonMain/**/*.kt") } to listOf(
            "android.", "androidx.", "java.", "platform.", "com.kunal26das.doom",
            "io.github.kunal26das.startup",
        ),
        fileTree("composeApp/src/commonMain/kotlin/com/kunal26das/doom/presentation") {
            include("**/*.kt")
        } to listOf("doom.engine", "com.kunal26das.doom.data", "io.github.kunal26das.startup"),
    )
    val engineSources = fileTree("engine/src/commonMain") { include("**/*.kt") }
    val engineServices = fileTree("engine/src/commonMain/kotlin/doom/engine") {
        listOf("runtime", "simulation", "rendering", "geometry", "configuration", "capture", "menu")
            .forEach { include("$it/**/*.kt") }
    }
    inputs.files(engineSources)
    rules.forEach { (sources, _) -> inputs.files(sources) }
    doLast {
        val violations = rules.flatMap { (sources, forbidden) ->
            sources.flatMap { source ->
                source.readLines().mapIndexedNotNull { index, line ->
                    if (line.trimStart().startsWith("//") || line.trimStart().startsWith("*")) null
                    else forbidden.firstOrNull { prefix ->
                        Regex("(?<![\\w.])${Regex.escape(prefix)}").containsMatchIn(line)
                    }?.let { "${source.name}:${index + 1}: forbidden dependency $it" }
                }
            }
        }
        val globalState = engineSources.flatMap { source ->
            source.readLines().mapIndexedNotNull { index, line ->
                val declaration = Regex("^(?:(?:private|internal|public)\\s+)?(?:lateinit\\s+)?var\\s+(?!DoomEngineCore\\.)")
                if (declaration.containsMatchIn(line)) "${source.name}:${index + 1}: mutable top-level engine state" else null
            }
        }
        val serviceCoupling = engineServices.flatMap { source ->
            source.readLines().mapIndexedNotNull { index, line ->
                when {
                    line.trimStart().startsWith("//") || line.trimStart().startsWith("*") -> null
                    Regex("\\b(DoomEngineCore|LegacyEngineRuntime)\\b").containsMatchIn(line) ->
                        "${source.name}:${index + 1}: service must depend on focused ports, not the engine core"
                    line.trim() == "import doom.engine.*" ->
                        "${source.name}:${index + 1}: service dependencies must be explicit imports"
                    else -> null
                }
            }
        }
        val stateBackReferences = engineSources.mapNotNull { source ->
            val signature = Regex("\\bclass\\s+\\w*State\\b[^{}]*\\bDoomEngineCore\\b")
            if (signature.containsMatchIn(source.readText()))
                "${source.name}: state holder must not receive the engine core"
            else null
        }
        val legacyBoundaryLeaks = engineSources.flatMap { source ->
            val forbidden = when {
                source.name.startsWith("p_") -> listOf("R_PointToAngle2", "R_PointInSubsector")
                source.name.startsWith("r_") -> listOf("NetUpdate", "TryRunTics")
                else -> emptyList()
            }
            source.readLines().mapIndexedNotNull { index, line ->
                forbidden.firstOrNull { Regex("\\b$it\\s*\\(").containsMatchIn(line) }?.let {
                    "${source.name}:${index + 1}: use the geometry service or injected render checkpoint instead of $it"
                }
            }
        }
        val failures = violations + globalState + serviceCoupling + stateBackReferences + legacyBoundaryLeaks
        check(failures.isEmpty()) { failures.joinToString("\n") }
    }
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(":verifyArchitecture")
    }
}
