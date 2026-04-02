pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.google.com")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "ToiletGen"

// === Android Modules ===
include(":android:app")

// Core
include(":android:core:ui")
include(":android:core:network")
include(":android:core:database")
include(":android:core:domain")
include(":android:core:common")

// Features
include(":android:feature:map")
include(":android:feature:auth")
include(":android:feature:toilet_details")
include(":android:feature:sos")
include(":android:feature:profile")
include(":android:feature:achievements")
include(":android:feature:yearly_report")
include(":android:feature:entertainment")
include(":android:feature:chat")
include(":android:feature:stamps")

// === Backend Modules ===
include(":backend:shared:domain-events")
include(":backend:shared:messaging")
include(":backend:shared:security")

include(":backend:api-gateway")
include(":backend:identity-service")
include(":backend:toilet-service")
include(":backend:sos-service")
include(":backend:gamification-service")
