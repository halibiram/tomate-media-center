// build.gradle.kts for domain module (Kotlin JVM library)
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// Required for Kotlin JVM modules that are part of an Android project
// to ensure they are correctly identified by AGP.
android {
    namespace = "com.halibiram.tomato.domain"
}


dependencies {
    // Coroutines for Flow and suspend functions
    implementation(libs.kotlinx.coroutines.core)

    // Javax Inject for @Inject annotation (optional, but common for use cases)
    implementation(libs.javax.inject)
}
