// build.gradle.kts for data module
plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20" // For DTOs
    // id("com.google.devtools.ksp") version "1.8.20-1.0.11" // If using Room entities directly here, or for Hilt
}

android {
    namespace = "com.halibiram.tomato.data"
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
    implementation(project(":core:common"))
    implementation(project(":core:network"))   // For HttpClient, network responses
    implementation(project(":core:database"))  // For DAOs, Entities
    implementation(project(":core:datastore")) // For preferences, if used by extensions data part
    implementation(project(":domain"))         // For Repository interfaces, Domain Models

    // Kotlinx Serialization (already in core:network but good to be explicit if used heavily for DTOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // Ktor (already in core:network, but if this module defines APIs with it)
    // implementation("io.ktor:ktor-client-core:2.3.0")

    // Room (already in core:database)
    // implementation("androidx.room:room-ktx:2.5.2")

    // Dagger/Hilt (if used for DI)
    // implementation("com.google.dagger:hilt-android:2.45")
    // kapt("com.google.dagger:hilt-compiler:2.45") // or ksp if Hilt supports it fully for your setup

    // For LruCache in CacheManager example
    implementation("androidx.collection:collection-ktx:1.2.0")
}
