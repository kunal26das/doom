plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.kunal26das.doom.app"
    compileSdk = providers.gradleProperty("doom.android.compileSdk").get().toInt()

    defaultConfig {
        applicationId = "com.kunal26das.doom"
        minSdk = providers.gradleProperty("doom.android.minSdk").get().toInt()
        targetSdk = providers.gradleProperty("doom.android.targetSdk").get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":composeApp"))
}
