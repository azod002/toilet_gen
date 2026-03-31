import com.android.build.gradle.BaseExtension

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<BaseExtension> {
    buildFeatures.compose = true
}
