import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.konan.target.HostManager
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

apply(from = rootProject.file("gradle/dependency-repositories.gradle.kts"))

val swiftRuntimeDirectory = providers.exec {
    commandLine("xcrun", "--find", "swift")
}.standardOutput.asText.map {
    File(it.trim()).parentFile.parentFile.resolve("lib/swift")
}

kotlin {
    compilerOptions {
        extraWarnings.set(true)
        allWarningsAsErrors.set(true)
    }
    android {
        namespace = "com.kunal26das.doom"
        compileSdk = providers.gradleProperty("doom.android.compileSdk").get().toInt()
        minSdk = providers.gradleProperty("doom.android.minSdk").get().toInt()
        withHostTest {}
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        lint { warningsAsErrors = true }
        androidResources.enable = true
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        if (HostManager.hostIsMac) {
            val sdkName = if (iosTarget.name == "iosArm64") "iphoneos" else "iphonesimulator"
            iosTarget.binaries.configureEach {
                linkerOpts("-L${swiftRuntimeDirectory.get().resolve(sdkName).absolutePath}")
            }
        }
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", "com.kunal26das.doom.ComposeApp")
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        compilerOptions.freeCompilerArgs.add("-Xpartial-linkage-loglevel=ERROR")
        outputModuleName.set("composeApp")
        browser {
            testTask {
                useKarma { useChromeHeadless() }
            }
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
        getByName("androidDeviceTest").dependencies {
            implementation(kotlin("test"))
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.junit)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

abstract class VerifyWebSkikoRuntime : DefaultTask() {
    @get:Input
    abstract val expectedVersion: Property<String>

    @get:Input
    abstract val kotlinVersions: ListProperty<String>

    @get:Input
    abstract val runtimeVersions: ListProperty<String>

    @TaskAction
    fun verify() {
        val kotlin = kotlinVersions.get()
        val runtime = runtimeVersions.get()
        val expected = listOf(expectedVersion.get())
        check(kotlin == expected && runtime == expected) {
            "Browser Skiko versions must match: Kotlin/Wasm=$kotlin, JavaScript/native runtime=$runtime. " +
                "Expected the verified runtime $expected. Update the UI dependencies and runtime pin together."
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
    expectedVersion.set(libs.skiko.web.runtime.map { it.versionConstraint.requiredVersion })
    kotlinVersions.set(
        configurations.named("wasmJsCompileClasspath")
            .flatMap { it.incoming.resolutionResult.rootComponent }
            .map { VerifyWebSkikoRuntime.versions(it, setOf("skiko-wasm-js")) }
    )
    runtimeVersions.set(
        configurations.named("wasmJsRuntimeClasspath")
            .flatMap { it.incoming.resolutionResult.rootComponent }
            .map { VerifyWebSkikoRuntime.versions(it, setOf("skiko-wasm-js")) }
    )
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
