pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "WatchTime"
include(":app")
include(":app-tv")
include(":auth:ui")
include(":core:ui")
include(":auth:domain")
include(":auth:data")
include(":core:utils")
include(":core:home")
include(":core:navigation")
include(":popular:ui")
include(":popular:data")
include(":core:network")
include(":popular:domain")
include(":popular:domain")
include(":discover:ui")
include(":discover:domain")
include(":discover:data")
include(":media:ui")
include(":media:domain")
include(":media:data")
include(":collections:data")
include(":collections:domain")
include(":core:room")
include(":collections:ui")
include(":profile:ui")
include(":profile:domain")
include(":profile:data")

// TV modules
include(":core:tv-ui")
include(":auth:tv-ui")
include(":popular:tv-ui")
include(":discover:tv-ui")
include(":media:tv-ui")
include(":collections:tv-ui")

