// build.gradle.kts for feature/extensions module
plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.halibiram.tomato.feature.extensions"
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
    implementation(project(":core:network")) // For ExtensionAPI to make network calls
    implementation(project(":core:datastore")) // For ExtensionAPI to save/load data
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

    // Scripting engine (examples, choose one or none if extensions are pre-compiled)
    // implementation("org.mozilla:rhino:1.7.14") // For JavaScript via Rhino
    // implementation("org.luaj:luaj-jse:3.0.1") // For Lua via Luaj

    // Dagger/Hilt (if used)
    // implementation("com.google.dagger:hilt-android:2.45")
    // kapt("com.google.dagger:hilt-compiler:2.45")
    // implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
}
