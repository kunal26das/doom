rootProject.name = "doom"

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

include(":composeApp")
include(":androidApp")
include(":domain")
include(":engine")
