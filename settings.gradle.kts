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
include(":app")
include(
    ":core",
    ":core:audio",
    ":core:cache",
    ":core:camera",
    ":core:common",
    ":core:data",
    ":core:network",
    ":core:uikit",
    ":core:worker"
)



include(
    ":core:network:api",
    ":core:network:firebase",
    ":core:network:supabase",
)

include(
    "core:network:firebase:auth",
    "core:network:supabase:auth",
    )

include(
    ":feature",
    ":feature:comments",
    ":feature:detailmusic",
    ":feature:favorites",
    ":feature:home",
    ":feature:musiclist",
    ":feature:navigation",
    ":feature:player",
    ":feature:profile",
    ":feature:searchmusic",
    "feature:settings",
    ":feature:splashscreen",
    )
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
