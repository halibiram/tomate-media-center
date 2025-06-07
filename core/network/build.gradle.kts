// build.gradle.kts for core/network module
plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20" // Example version
}

android {
    namespace = "com.halibiram.tomato.core.network"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":core:common")) // Depends on core:common

    // Ktor
    implementation("io.ktor:ktor-client-core:2.3.0") // Example version
    implementation("io.ktor:ktor-client-cio:2.3.0") // Example for JVM, use -android for Android
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    implementation("io.ktor:ktor-client-logging:2.3.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0") // Example version
}
