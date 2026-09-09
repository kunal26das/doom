import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "com.kunal26das.doom"
        compileSdk = providers.gradleProperty("doom.android.compileSdk").get().toInt()
        minSdk = providers.gradleProperty("doom.android.minSdk").get().toInt()
        // Compose Multiplatform resources (the bundled WAD) ride through
        // Android resources/assets of this KMP library module.
        androidResources.enable = true
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        compilerOptions.freeCompilerArgs.add("-Xpartial-linkage-loglevel=ERROR")
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(project(":engine"))
            implementation(libs.startup)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material)
            implementation(libs.compose.ui)
            implementation(libs.compose.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            api(libs.androidx.activity.compose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

// Upgrade the inherited browser dependencies without declaring them twice.
// Native targets keep the catalog versions; the browser stays matched with Skiko below.
dependencies {
    constraints {
        val browserImplementation = kotlin.sourceSets.named("wasmJsMain").get().implementationConfigurationName
        val browserComposeVersion = providers.gradleProperty("doom.compose.web.version").get()
        listOf(libs.compose.material, libs.compose.resources).forEach { dependency ->
            addProvider(browserImplementation, dependency) {
                version { require(browserComposeVersion) }
                because("The browser renderer needs the Compose/Skiko graphics-context lifetime fix.")
            }
        }
    }
}

// The 1.12 Gradle plugin otherwise supplies a 0.150 browser runtime independently
// of wasmJsMain's resolved Kotlin libraries. Both halves must use the same Skiko ABI.
configurations.matching { it.name == "COMPOSE_SKIKO_JS_WASM_RUNTIME" }.configureEach {
    dependencies.addLater(project.dependencies.variantOf(libs.skiko.web.runtime) {
        classifier("skiko-runtime")
        artifactType("jar")
    })
}

abstract class VerifyWebSkikoRuntime : DefaultTask() {
    @get:Input
    abstract val kotlinVersions: ListProperty<String>

    @get:Input
    abstract val runtimeVersions: ListProperty<String>

    @TaskAction
    fun verify() {
        val kotlin = kotlinVersions.get()
        val runtime = runtimeVersions.get()
        check(kotlin.size == 1 && runtime.size == 1 && kotlin == runtime) {
            "Browser Skiko versions must match: Kotlin/Wasm=$kotlin, JavaScript/native runtime=$runtime. " +
                "Update the browser UI dependencies and packaged Skiko runtime together."
        }
    }

    companion object {
        fun versions(root: ResolvedComponentResult, moduleNames: Set<String>): List<String> {
            val pending = ArrayDeque<ResolvedComponentResult>()
            val visited = mutableSetOf<ComponentIdentifier>()
            val versions = mutableSetOf<String>()
            pending.add(root)
            while (pending.isNotEmpty()) {
                val component = pending.removeFirst()
                if (!visited.add(component.id)) continue
                val id = component.id as? ModuleComponentIdentifier
                if (id?.group == "org.jetbrains.skiko" && id.module in moduleNames) versions.add(id.version)
                component.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach {
                    pending.add(it.selected)
                }
            }
            return versions.sorted()
        }
    }
}

val verifyWebSkikoRuntime = tasks.register<VerifyWebSkikoRuntime>("verifyWebSkikoRuntime") {
    group = "verification"
    description = "Reject mismatched browser Skiko libraries and packaged rendering runtime."
    kotlinVersions.set(
        configurations.named("wasmJsCompileClasspath")
            .flatMap { it.incoming.resolutionResult.rootComponent }
            .map { VerifyWebSkikoRuntime.versions(it, setOf("skiko-wasm-js")) }
    )
}
configurations.matching { it.name == "COMPOSE_SKIKO_JS_WASM_RUNTIME" }.configureEach {
    val versions = incoming.resolutionResult.rootComponent.map {
        VerifyWebSkikoRuntime.versions(it, setOf("skiko-wasm-js", "skiko-js-wasm-runtime"))
    }
    verifyWebSkikoRuntime.configure { runtimeVersions.set(versions) }
}
tasks.matching {
    it.name == "wasmJsBrowserDistribution" || it.name == "compileProductionExecutableKotlinWasmJs"
}.configureEach {
    dependsOn(verifyWebSkikoRuntime)
}

compose.desktop {
    application {
        mainClass = "com.kunal26das.doom.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DOOM"
            packageVersion = "1.0.0"
        }
    }
}

val jvmMainCompilation = kotlin.targets.getByName("jvm").compilations.getByName("main")
tasks.withType<Test>().configureEach {
    systemProperty("doom.test.wad", rootProject.file("composeApp/src/commonMain/composeResources/files/doom1.wad").absolutePath)
    doFirst { systemProperty("doom.test.classpath", classpath.asPath) }
}

tasks.register<JavaExec>("runHeadless") {
    group = "verification"
    description = "Run DOOM deterministically without a window or user save files."
    mainClass.set("com.kunal26das.doom.HeadlessKt")
    classpath(jvmMainCompilation.output.allOutputs, jvmMainCompilation.runtimeDependencyFiles)
    workingDir(rootProject.projectDir)
}
