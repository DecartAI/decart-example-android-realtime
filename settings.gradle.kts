pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "decart-example-android"
include(":app")

includeBuild("../decart-android-sdk") {
    dependencySubstitution {
        substitute(module("ai.decart:sdk")).using(project(":sdk"))
    }
}
