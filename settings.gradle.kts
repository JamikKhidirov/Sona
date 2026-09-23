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
    ":core:data:apps",
    ":core:data:battery",
    ":core:data:bluetooth",
    ":core:data:browser",
    ":core:data:clipboard",
    ":core:data:contacts",
    ":core:data:device",
    ":core:data:display",
    ":core:data:documents",
    ":core:data:downloads",
    ":core:data:gallery",
    ":core:data:location",
    ":core:data:media",
    ":core:data:network",
    ":core:data:notifications",
    ":core:data:performance",
    ":core:data:permissions",
    ":core:data:sensors",
    ":core:data:sms",
    ":core:data:storage",
    ":core:data:usage",
    ":core:data:wifi",
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
    ":feature:favorits",
    ":feature:home",
    ":feature:musiclist",
    ":feature:player",
    ":feature:profile",
    ":feature:searchmusic",
    "feature:settings",
    ":feature:splashscreen",
    )