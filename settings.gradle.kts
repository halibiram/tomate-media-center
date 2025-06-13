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

rootProject.name = "Tomato Media Center"
include(":app")

// Core modules
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:player")

// Feature modules
include(":feature:home")
include(":feature:search")
include(":feature:player")
include(":feature:downloads")
include(":feature:bookmarks")
include(":feature:settings")
include(":feature:extensions")

// Data & Domain
include(":data")
include(":domain")
