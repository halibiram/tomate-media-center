# Development Setup Guide

This guide provides instructions for setting up the development environment for Tomato Media Center.

## Prerequisites

- **Android Studio:** Latest stable version recommended (e.g., Android Studio Iguana | 2023.2.1 or newer). Download from the [Android Developer website](https://developer.android.com/studio).
- **JDK (Java Development Kit):** JDK 17. Android Studio usually comes with an embedded JDK, but ensure your project is configured to use JDK 17. This is typically set in `File > Project Structure > SDK Location > Gradle Settings` or via your system's `JAVA_HOME` environment variable.
- **Android SDK:**
    - Target SDK: API Level 34 (Android 14) or as specified in the project's `build.gradle.kts` (`targetSdk`).
    - Compile SDK: API Level 34 (Android 14) or as specified (`compileSdk`).
    - Min SDK: API Level 24 (Android 7.0 Nougat) or as specified (`minSdk`).
    - Ensure you have the necessary SDK Platforms and Build-Tools installed via Android Studio's SDK Manager.
- **Git:** For version control. Download from [git-scm.com](https://git-scm.com/).

## Getting the Code

1.  **Clone the Repository:**
    ```bash
    git clone https://example.com/your-repo/tomato.git # Replace with the actual repository URL
    cd tomato
    ```
2.  **Import into Android Studio:**
    - Open Android Studio.
    - Select "Open" (or "Open an Existing Project").
    - Navigate to and select the cloned `tomato` directory.
    - Android Studio will import the project and Gradle will sync. This might take a few minutes.

## Build Configuration

- **`local.properties`:** This file is typically not version-controlled (`.gitignore`). If any API keys or specific local configurations are required for development builds (e.g., TMDB API key for fetching movie metadata from a non-extension source), you might need to create/update this file. Example:
  ```properties
  # Sample local.properties content (if needed)
  # TOMATO_API_KEY="your_api_key_here"
  ```
  Check with the project maintainers if any specific local properties are required.
- **Build Variants:** The project uses standard Android build variants (e.g., `debug`, `release`). You can select the active build variant in Android Studio via `Build > Select Build Variant...`.

## First Build

1.  **Gradle Sync:** Ensure Gradle sync completes successfully after opening the project. If not, check the "Build" output panel for errors.
2.  **Build from Command Line (Optional but Recommended):**
    ```bash
    ./gradlew assembleDebug
    ```
    This command builds the debug version of the application.
3.  **Build from Android Studio:**
    - Click `Build > Make Project` or the "Make Project" button (hammer icon).
    - Alternatively, run a specific build task from the "Gradle" tool window.

## Running on Emulator/Device

1.  **Set up an Emulator:**
    - In Android Studio, go to `Tools > Device Manager`.
    - Create a new Virtual Device (AVD) targeting an appropriate API level (e.g., API 30+).
2.  **Connect a Physical Device:**
    - Enable Developer Options and USB Debugging on your Android device.
    - Connect the device to your computer via USB. Authorize debugging if prompted.
3.  **Run the App:**
    - Select your target device (emulator or physical) from the device dropdown in Android Studio.
    - Click the "Run 'app'" button (green play icon) or `Run > Run 'app'`.

## Running Tests

- **Unit Tests:**
  ```bash
  ./gradlew testDebugUnitTest
  ```
  (Runs unit tests for all modules. To run for a specific module: `./gradlew :moduleName:testDebugUnitTest`)
- **Android Instrumented Tests:**
  Ensure an emulator is running or a device is connected.
  ```bash
  ./gradlew connectedDebugAndroidTest
  ```
  (Runs instrumented tests for all modules. To run for a specific module: `./gradlew :moduleName:connectedDebugAndroidTest`)

## (Optional) Building and Using the Dummy Extension APK

If the project includes the `extensions/dummy_success_provider` module for testing the extension system:

1.  **Build the Dummy Extension APK:**
    This module might need to be configured to output an APK (e.g., by temporarily applying `com.android.application` plugin or via a custom Gradle task).
    ```bash
    ./gradlew :extensions:dummy_success_provider:assembleDebug
    # The exact task name might vary based on module configuration.
    ```
2.  **Locate the APK:**
    Typically found in `extensions/dummy_success_provider/build/outputs/apk/debug/`.
3.  **Install on Device/Emulator:**
    You can sideload this APK onto your test device/emulator. Then, use the main Tomato app's "Install Extension" feature (the FAB on the Extensions screen) to pick this APK file.

## Common Issues & Troubleshooting

- **Gradle Sync Errors:**
    - Check internet connection.
    - Ensure you have the correct JDK version configured for Gradle.
    - Try `File > Invalidate Caches / Restart...` in Android Studio.
    - Look for specific error messages in the "Build" output panel.
- **Missing SDK Components:** Use the SDK Manager in Android Studio to install any missing platforms, build-tools, or other components indicated in error messages.
- **Emulator Issues:** Ensure HAXM (for Intel) or AMD Hypervisor is correctly installed and enabled. Check AVD configuration.

## IDE Configuration (Optional)

- **Plugins:** Consider plugins like "Ktlint" or "Checkstyle" for code style consistency if the project uses them.
- **Code Style:** Ensure your IDE is configured to use the project's Kotlin code style (often default or specified in `.editorconfig`).
md
File 'docs/development/setup.md' overwritten successfully.
