// build.gradle.kts for core/database module
plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.8.20-1.0.11" // KSP for Room
}

android {
    namespace = "com.halibiram.tomato.core.database"
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

    // Room
    val roomVersion = "2.5.2" // Example version
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    // Optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")
    // Optional - Test helpers
    // testImplementation("androidx.room:room-testing:$roomVersion")

    // Kotlin Coroutines for Flow
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1") // Or core if not Android specific
}
