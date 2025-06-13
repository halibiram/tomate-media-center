# Tomato Media Center (Placeholder Name)

Tomato is a modern, extensible media center application for Android, built with Kotlin and Jetpack Compose. It aims to provide a seamless experience for browsing and playing local media, as well as extending content sources through a plugin-like extension system.

## ✨ Features

- **Modern UI:** Built with Jetpack Compose and Material 3 for a clean and responsive user interface.
- **Media Playback:**
    - Robust video playback using `androidx.media3` (ExoPlayer).
    - Gesture controls for brightness, volume (TODO), and seeking.
    - Support for subtitle tracks and audio track selection.
    - Background playback with notification controls.
- **Casting:** Stream media to Google Cast compatible devices.
- **Local Library:** (Implicit, as foundation for movies/series) Scan and manage local media files.
- **Movie & Series Browsing:**
    - Discover popular and trending movies.
    - (Series browsing is foundational but not explicitly detailed for Home screen yet).
- **Search:** Find movies from your local library and (soon) from installed extensions.
- **Downloads:** Download media for offline viewing with background progress and management.
- **Bookmarks:** Save your favorite movies and series for quick access.
- **Settings:** Customize application appearance (themes), player behavior, and data management.
- **Extension System (Basic):**
    - Install and manage extensions (currently via sideloading APKs).
    - Extensions can provide new content sources (e.g., `MovieProviderExtension` for popular movies and search).
    - Real APK parsing for manifest details and class loading via `DexClassLoader`.

## 🛠️ Technology Stack

- **Kotlin:** Primary programming language.
- **Jetpack Compose:** For building the UI.
- **Material 3:** For UI components and theming.
- **Coroutines & Flow:** For asynchronous operations and reactive data streams.
- **Hilt:** For dependency injection.
- **Jetpack Navigation (Compose):** For navigating between screens.
- **AndroidX Media3 (ExoPlayer):** For media playback and media session management.
- **Google Cast SDK:** For Chromecast support.
- **Room:** For local database (bookmarks, downloads, extension metadata).
- **DataStore Preferences:** For user settings and application preferences.
- **WorkManager:** For background download tasks.
- **Retrofit & Ktor:** (Ktor is currently set up) For network operations.
- **Coil:** For image loading.
- **Gradle & Kotlin DSL:** For building the project.
- **GitHub Actions:** For CI/CD.

## 🚀 Setup & Build

1.  **Prerequisites:**
    *   Android Studio (latest stable version, e.g., Iguana, Hedgehog).
    *   JDK 17.
    *   Android SDK (target/compile SDK, e.g., 34).

2.  **Clone the Repository:**
    ```bash
    git clone https://example.com/your-repo/tomato.git # Replace with actual repo URL
    cd tomato
    ```

3.  **Import into Android Studio:**
    *   Open Android Studio.
    *   Select "Open an Existing Project" and choose the cloned directory.
    *   Wait for Gradle sync to complete.

4.  **Build Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```

5.  **Install Debug APK:**
    ```bash
    ./gradlew installDebug
    ```
    Alternatively, run directly from Android Studio on an emulator or connected device.

## 📸 Screenshots (Future)

<!-- Add placeholders for screenshots once UI is more finalized -->
<!--
![Home Screen](docs/images/screenshots/home.png)
![Player Screen](docs/images/screenshots/player.png)
![Settings Screen](docs/images/screenshots/settings.png)
-->

## 🤝 Contributing

Contributions are welcome! Please read our [CONTRIBUTING.md](docs/development/contributing.md) guide to get started.

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE). <!-- Assuming Apache 2.0, update if different -->

---

*This README is a placeholder and should be updated as the project evolves.*
