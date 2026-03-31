plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    val libs = versionCatalogs.named("libs")
    "implementation"(libs.findLibrary("ktor-server-core").get())
    "implementation"(libs.findLibrary("ktor-server-netty").get())
    "implementation"(libs.findLibrary("ktor-server-content-negotiation").get())
    "implementation"(libs.findLibrary("ktor-server-status-pages").get())
    "implementation"(libs.findLibrary("ktor-server-call-logging").get())
    "implementation"(libs.findLibrary("ktor-serialization-json").get())
    "implementation"(libs.findLibrary("kotlinx-coroutines-core").get())
    "implementation"(libs.findLibrary("kotlinx-serialization-json").get())
    "implementation"(libs.findLibrary("koin-core").get())
    "implementation"(libs.findLibrary("koin-ktor").get())
    "implementation"(libs.findLibrary("logback").get())
}
