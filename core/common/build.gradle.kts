// build.gradle.kts for core/common module
plugins {
    id("com.android.library") // Or other appropriate plugin
    kotlin("android")
}

android {
    namespace = "com.halibiram.tomato.core.common"
    compileSdk = 33 // Example SDK version

    defaultConfig {
        minSdk = 24
        // No testInstrumentationRunner for library modules unless they have instrumented tests
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
    // Add common dependencies, e.g., Kotlin stdlib, coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1") // Example
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1") // For BaseViewModel example
}
