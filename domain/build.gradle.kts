// build.gradle.kts for domain module (Kotlin JVM library)
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20" // Ensure version consistency
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    // Kotlin Standard Library (already included by the plugin but good for clarity)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Coroutines for Flow and suspend functions
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1") // Example version

    // Javax Inject for @Inject annotation (optional, but common for use cases)
    implementation("javax.inject:javax.inject:1")
}
