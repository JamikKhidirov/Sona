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

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Sona"

// app
include(":app")

// core
include(":core")
include(":core:common")
include(":core:data")
include(":core:network")
include(":core:uikit")
include(":core:worker")

// core/network
include(":core:network:api")
include(":core:network:api:music")
include(":core:network:firebase")
include(":core:network:firebase:auth")
include(":core:network:supabase")
include(":core:network:supabase:auth")

// core/data
include(":core:data:alarms")
include(":core:data:apps")
include(":core:data:battery")
include(":core:data:bluetooth")
include(":core:data:browser")
include(":core:data:clipboard")
include(":core:data:contacts")
include(":core:data:device")
include(":core:data:display")
include(":core:data:documents")
include(":core:data:downloads")
include(":core:data:gallery")
include(":core:data:hardware")
include(":core:data:location")
include(":core:data:wifi")
include(":core:data:sms")
include(":core:data:vpn")
include(":core:data:usb")
include(":core:data:nfc")
include(":core:data:media")
include(":core:data:network")
include(":core:data:sound")
include(":core:data:storage")
include(":core:data:usage")
include(":core:data:sensors")
include(":core:data:permissions")
include(":core:data:notifications")
include(":core:data:performance")

// core/audio
include(":core:audio")
include(":core:audio:api")
include(":core:audio:impl")

// core/cache
include(":core:cache")
include(":core:cache:api")
include(":core:cache:impl")

// core/camera
include(":core:camera")
include(":core:camera:api")
include(":core:camera:impl")

// core/uikit
include(":core:uikit:screens")
include(":core:uikit:components")

// core/worker
include(":core:worker:sync")

// feature
include(":feature")

// feature/navigation
include(":feature:navigation:api")
include(":feature:navigation:impl")

// feature/comments
include(":feature:comments:api")
include(":feature:comments:impl")

// feature/detailmusic
include(":feature:detailmusic:api")
include(":feature:detailmusic:impl")

// feature/favorites
include(":feature:favorites:api")
include(":feature:favorites:impl")

// feature/home
include(":feature:home:api")
include(":feature:home:impl")

// feature/musiclist
include(":feature:musiclist:api")
include(":feature:musiclist:impl")

// feature/player
include(":feature:player:api")
include(":feature:player:impl")

// feature/profile
include(":feature:profile:api")
include(":feature:profile:impl")

// feature/searchmusic
include(":feature:searchmusic:api")
include(":feature:searchmusic:impl")

// feature/settings
include(":feature:settings:api")
include(":feature:settings:impl")

// feature/splashscreen
include(":feature:splashscreen:api")
include(":feature:splashscreen:impl")