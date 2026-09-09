import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

apply(from = rootProject.file("gradle/dependency-repositories.gradle.kts"))

kotlin {
    explicitApi()
    compilerOptions {
        extraWarnings.set(true)
        allWarningsAsErrors.set(true)
    }
    android {
        namespace = "com.kunal26das.doom.engine"
        compileSdk = providers.gradleProperty("doom.android.compileSdk").get().toInt()
        minSdk = providers.gradleProperty("doom.android.minSdk").get().toInt()
        withHostTest {}
        lint { warningsAsErrors = true }
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                useKarma { useChromeHeadless() }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.withType<Test>().configureEach {
    systemProperty("doom.test.wad", rootProject.file("composeApp/src/commonMain/composeResources/files/doom1.wad").absolutePath)
    doFirst { systemProperty("doom.test.classpath", classpath.asPath) }
}
