import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.toiletgen.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.toiletgen.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "MAPKIT_API_KEY", "\"${localProps.getProperty("MAPKIT_API_KEY", "")}\"")
        buildConfigField("String", "API_BASE_URL", "\"${localProps.getProperty("API_BASE_URL", "http://localhost:8080/")}\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Compose
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Core modules
    implementation(project(":android:core:ui"))
    implementation(project(":android:core:network"))
    implementation(project(":android:core:database"))
    implementation(project(":android:core:domain"))
    implementation(project(":android:core:common"))

    // Feature modules
    implementation(project(":android:feature:map"))
    implementation(project(":android:feature:auth"))
    implementation(project(":android:feature:toilet_details"))
    implementation(project(":android:feature:sos"))
    implementation(project(":android:feature:profile"))
    implementation(project(":android:feature:achievements"))
    implementation(project(":android:feature:yearly_report"))
    implementation(project(":android:feature:entertainment"))
    implementation(project(":android:feature:chat"))

    // Compose
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Yandex MapKit
    implementation(libs.yandex.mapkit)
}
