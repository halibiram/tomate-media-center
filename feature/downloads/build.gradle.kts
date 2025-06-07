// build.gradle.kts for feature/downloads module
plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.halibiram.tomato.feature.downloads"
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
    implementation(project(":core:database")) // For DownloadDao
    implementation(project(":core:network")) // For Ktor client in worker
    implementation(project(":core:domain"))

    // AndroidX & Compose
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1") // Example version

    // Ktor (if used directly in Worker, though could be wrapped in core:network)
    // implementation("io.ktor:ktor-client-android:2.3.0")


    // Dagger/Hilt (if used, especially for HiltWorkerFactory)
    // implementation("com.google.dagger:hilt-android:2.45")
    // kapt("com.google.dagger:hilt-compiler:2.45")
    // implementation("androidx.hilt:hilt-work:1.0.0")
    // kapt("androidx.hilt:hilt-compiler:1.0.0") // Hilt compiler for work
    // implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
}
