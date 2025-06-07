// build.gradle.kts for feature/player module
plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.halibiram.tomato.feature.player"
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:player")) // Key dependency for player functionality
    implementation(project(":core:domain")) // For media metadata, etc.

    // AndroidX & Compose
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.foundation:foundation:1.4.3") // For gestures
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")
    implementation("androidx.compose.ui:ui-viewbinding:1.4.3") // If using AndroidView with view binding

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // Media3 (if interacting with it directly in the feature module, though core:player should encapsulate)
    // implementation("androidx.media3:media3-ui:1.1.1")

    // Dagger/Hilt (if used)
    // implementation("com.google.dagger:hilt-android:2.45")
    // kapt("com.google.dagger:hilt-compiler:2.45")
    // implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
}
