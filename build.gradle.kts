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
    val compatibilityInventory = file("gradle/engine-compatibility-sources.txt")
    val engineSourceRoot = file("engine/src/commonMain/kotlin/doom/engine")
    val publicApiFiles = setOf(
        "DoomClock.kt", "DoomConstants.kt", "DoomEngine.kt", "DoomError.kt",
        "DoomHost.kt", "DoomInput.kt", "DoomJoystickInput.kt", "DoomKeyInput.kt",
        "DoomMetrics.kt", "DoomMouseInput.kt", "DoomMusic.kt", "DoomSoundEffects.kt",
        "DoomStorage.kt", "DoomVideo.kt", "ISoundDriver.kt",
    )
    inputs.file(compatibilityInventory)
    inputs.files(engineSources)
    rules.forEach { (sources, _) -> inputs.files(sources) }
    doLast {
        val compatibilityPaths = compatibilityInventory.readLines()
            .map { it.substringBefore('#').trim() }.filter { it.isNotEmpty() }
        check(compatibilityPaths.size == compatibilityPaths.distinct().size) {
            "Duplicate entries in the engine compatibility inventory"
        }
        val sourcePaths = engineSources.associateBy { it.relativeTo(engineSourceRoot).invariantSeparatorsPath }
        check(compatibilityPaths.all { it in sourcePaths }) {
            "Stale engine compatibility paths: " + compatibilityPaths.filterNot { it in sourcePaths }
        }
        val compatibility = compatibilityPaths.toSet()
        val publicApiLeaks = sourcePaths.keys.filter { '/' !in it && it !in publicApiFiles }
            .map { "$it: keep implementation files in their functional package; the root is the public API" }
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
        val serviceCoupling = engineSources.flatMap { source ->
            val path = source.relativeTo(engineSourceRoot).invariantSeparatorsPath
            source.readLines().mapIndexedNotNull { index, line ->
                when {
                    line.trimStart().startsWith("//") || line.trimStart().startsWith("*") -> null
                    path !in compatibility && Regex("\\b(DoomEngineCore|LegacyEngineRuntime)\\b").containsMatchIn(line) ->
                        "$path:${index + 1}: services and models must depend on focused ports, not the engine core"
                    Regex("^import doom\\.engine(?:\\.[\\w]+)*\\.\\*$").matches(line.trim()) ->
                        "$path:${index + 1}: engine dependencies must be explicit imports"
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
            val path = source.relativeTo(engineSourceRoot).invariantSeparatorsPath
            val forbidden = when {
                path.startsWith("gameplay/") || path.startsWith("world/") || path.startsWith("savegame/") ->
                    listOf("rPointToAngle2", "rPointInSubsector")
                path.startsWith("rendering/") -> listOf("netUpdate", "tryRunTics")
                else -> emptyList()
            }
            source.readLines().mapIndexedNotNull { index, line ->
                forbidden.firstOrNull { Regex("\\b$it\\s*\\(").containsMatchIn(line) }?.let {
                    "${source.name}:${index + 1}: use the geometry service or injected render checkpoint instead of $it"
                }
            }
        }
        val failures = violations + globalState + serviceCoupling + stateBackReferences + legacyBoundaryLeaks + publicApiLeaks
        check(failures.isEmpty()) { failures.joinToString("\n") }
    }
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(":verifyArchitecture")
    }
}
