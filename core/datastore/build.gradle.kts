// build.gradle.kts for core/datastore module
plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20" // For Kotlinx Serialization
}

android {
    namespace = "com.halibiram.tomato.core.datastore"
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

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0") // Example version

    // Proto DataStore (if you were to use it, add this)
    // implementation  "androidx.datastore:datastore-core:1.0.0"

    // Kotlinx Serialization (for custom serializers with Proto or for Preferences if storing complex objects)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}
