import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

apply(from = rootProject.file("gradle/dependency-repositories.gradle.kts"))

kotlin {
    compilerOptions {
        extraWarnings.set(true)
        allWarningsAsErrors.set(true)
    }
    android {
        namespace = "com.kunal26das.doom.domain"
        compileSdk = providers.gradleProperty("doom.android.compileSdk").get().toInt()
        minSdk = providers.gradleProperty("doom.android.minSdk").get().toInt()
        withHostTest {}
        lint { warningsAsErrors = true }
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
