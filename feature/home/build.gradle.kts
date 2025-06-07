// build.gradle.kts for feature/home module
plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.android") // Ensure this is applied for Compose
}

android {
    namespace = "com.halibiram.tomato.feature.home"
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
        compose = true // Enable Compose for this module
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3" // Example, use appropriate version
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain")) // Or specific core modules like :core:network, :core:database if needed directly
    // implementation(project(":app")) // This would be an anti-pattern for feature modules

    // AndroidX & Compose
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.material3:material3:1.1.1") // Or Material 2
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // Dagger/Hilt (if used)
    // implementation("com.google.dagger:hilt-android:2.45")
    // kapt("com.google.dagger:hilt-compiler:2.45")
    // implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
}
