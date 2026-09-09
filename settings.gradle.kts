rootProject.name = "doom"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupByRegex("androidx(\\..*)?")
                includeGroupByRegex("com\\.android(\\..*)?")
                includeGroupByRegex("com\\.google(\\..*)?")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Gradle 9.7.1 marks both central repository configuration and its getter incubating.
    @Suppress("UnstableApiUsage")
    repositories {
        google {
            mavenContent {
                includeGroupByRegex("androidx(\\..*)?")
                includeGroupByRegex("com\\.android(\\..*)?")
                includeGroupByRegex("com\\.google(\\..*)?")
            }
        }
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/cmp/dev") {
            content { includeGroupByRegex("org\\.jetbrains\\.compose(\\..*)?") }
        }
    }
}

include(":composeApp")
include(":androidApp")
include(":domain")
include(":engine")
