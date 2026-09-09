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
