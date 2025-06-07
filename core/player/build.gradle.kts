// build.gradle.kts for core/player module
plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.halibiram.tomato.core.player"
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
    implementation(project(":core:datastore")) // May need player preferences

    // Media3 (ExoPlayer, UI, Session)
    val media3Version = "1.1.1" // Example version
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")

    // Google Cast SDK (optional, if used)
    // implementation("com.google.android.gms:play-services-cast-framework:21.3.0") // Example version
}
