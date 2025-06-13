# Tomato Extension API

This document outlines the API for creating extensions for the Tomato application. Extensions allow third-party developers to add new functionalities and content sources.

## Introduction

The Tomato Extension API is designed to allow external modules (typically packaged as separate APKs) to integrate with the main Tomato application. These extensions can provide various functionalities, with an initial focus on supplying media content like movies and series.

The host application (Tomato) loads these extensions at runtime, parses their manifests for necessary metadata, and interacts with them through defined Kotlin interfaces.

## Core Concepts

-   **Extension Manifest (`AndroidManifest.xml`):** Each extension APK must declare its capabilities and metadata within its `AndroidManifest.xml` file using specific `<meta-data>` tags. This is how the Tomato app discovers and understands the extension.
-   **Permissions:** Extensions run within the Tomato app's process but are loaded via `DexClassLoader`. Future enhancements may involve a more sandboxed environment or permission model for extensions. For now, extensions should be treated as semi-trusted.
-   **API Versioning:** Both the host app and extensions use an API version number to ensure compatibility. Extensions targeting an API version newer than what the host supports may not be loaded.
-   **Content Provider Model:** The primary way extensions currently provide data is by implementing specific "provider" interfaces, like `MovieProviderExtension`.

## Core Interfaces

### 1. `ExtensionManifest`

This is the fundamental interface that all extension *instances* (the objects loaded from the extension's code) must effectively provide details for. These details are primarily read by the host app from the extension APK's `AndroidManifest.xml`.

**Properties defined in `interface ExtensionManifest`:**

-   `id: String`: A unique identifier for the extension. For APK-based extensions, this **must be the package name** declared in the extension's `AndroidManifest.xml`.
-   `name: String`: A human-readable name for the extension.
-   `version: String`: The version string of the extension (e.g., "1.0.2").
-   `author: String`: The name of the extension's author or organization.
-   `description: String?`: A brief description of what the extension does.
-   `apiVersion: Int`: The version of this Tomato Extension API that the extension targets (e.g., `1`, corresponding to `CURRENT_HOST_EXTENSION_API_VERSION`).
-   `className: String`: The fully qualified name of the main class within the extension APK that implements one or more specific provider interfaces (e.g., `com.example.myextension.MyMovieProvider`).

### 2. `MovieProviderExtension` (Example Content Provider)

This interface allows an extension to provide movie listings (e.g., popular, search results). It extends `ExtensionManifest`, meaning implementations must also satisfy the manifest contract (though these details are usually pulled from the APK manifest by the host).

**Interface Definition:**
```kotlin
interface MovieProviderExtension : ExtensionManifest {
    suspend fun getPopularMovies(page: Int): List<MovieSourceItem>
    suspend fun searchMovies(query: String, page: Int): List<MovieSourceItem>
    // suspend fun getMovieDetails(movieId: String): MovieSourceItemDetails? // Example for future
}
```

-   **`getPopularMovies(page: Int): List<MovieSourceItem>`**:
    -   Should return a list of popular movies for the given page number.
    -   Pagination starts at page 1.
-   **`searchMovies(query: String, page: Int): List<MovieSourceItem>`**:
    -   Should return a list of movies matching the search query for the given page.

### 3. `MovieSourceItem` Data Class

This data class defines the structure of movie information that `MovieProviderExtension` methods should return.

**Data Class Definition:**
```kotlin
data class MovieSourceItem(
    val id: String, // Unique ID for this item within the extension's context
    val title: String,
    val posterUrl: String?, // URL to the movie's poster image
    val year: String?,      // Release year, e.g., "2023"
    val sourceData: Map<String, String> = emptyMap() // Optional map for extension-specific data needed later (e.g., to resolve playback URLs)
)
```

## How to Create an Extension (High-Level Guide)

1.  **Set up an Android Library Module:**
    -   This module will be built into an APK. (Using an Application module with no launcher activity is also an option for easier APK generation).
    -   Ensure the module's `package` attribute in its `AndroidManifest.xml` will serve as the unique `id` for your extension.
2.  **Add Tomato Extension API Dependency:**
    -   Your extension module will need to depend on the Tomato Extension API definition (e.g., the module or library where `ExtensionManifest.kt`, `MovieProviderExtension.kt`, etc., are defined).
3.  **Implement Extension Interfaces:**
    -   Create a public class in your extension module. This class name is your `className`.
    -   This class must implement one or more provider interfaces (e.g., `MovieProviderExtension`).
    -   Implement all required methods (e.g., `getPopularMovies`, `searchMovies`).
    -   The class should have a public no-argument constructor for instantiation by `DexClassLoader`.
4.  **Declare in `AndroidManifest.xml`:**
    -   In your extension module's `AndroidManifest.xml`, within the `<application>` tag, add `<meta-data>` tags to declare your extension to the Tomato host app.
    -   **Required Meta-Data:**
        -   `com.halibiram.tomato.EXTENSION_CLASS_NAME`: Value should be the fully qualified name of your main extension class (e.g., `com.example.myextension.MyMovieProvider`).
        -   `com.halibiram.tomato.EXTENSION_API_VERSION`: Value should be an integer representing the Tomato Extension API version your extension targets (e.g., `"1"`).
    -   **Recommended Meta-Data (will also be read from here):**
        -   `com.halibiram.tomato.EXTENSION_NAME`: Human-readable name.
        -   `com.halibiram.tomato.EXTENSION_VERSION_NAME`: Your extension's version (e.g., "1.0.3").
        -   `com.halibiram.tomato.EXTENSION_AUTHOR`: Author's name.
        -   `com.halibiram.tomato.EXTENSION_DESCRIPTION`: Brief description.
    -   **Example `AndroidManifest.xml` for an extension:**
        ```xml
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
            package="com.example.myextension"> <!-- This becomes manifest.id -->
            <application android:label="My Extension Label"> <!-- Label can be a fallback for name -->
                <meta-data android:name="com.halibiram.tomato.EXTENSION_CLASS_NAME" android:value="com.example.myextension.MyMovieProvider"/>
                <meta-data android:name="com.halibiram.tomato.EXTENSION_API_VERSION" android:value="1"/>
                <meta-data android:name="com.halibiram.tomato.EXTENSION_NAME" android:value="My Awesome Movie Source"/>
                <meta-data android:name="com.halibiram.tomato.EXTENSION_VERSION_NAME" android:value="1.2.0"/>
                <meta-data android:name="com.halibiram.tomato.EXTENSION_AUTHOR" android:value="Dev Extraordinaire"/>
                <meta-data android:name="com.halibiram.tomato.EXTENSION_DESCRIPTION" android:value="Provides a curated list of awesome movies."/>
            </application>
        </manifest>
        ```
5.  **Build the APK:** Compile your extension module into an APK file.

## Loading Mechanism (Host App)

The Tomato host application will:
1.  Allow users to select an APK file using the system file picker.
2.  Copy the selected APK to its internal storage for stability and access.
3.  Use Android's `PackageManager.getPackageArchiveInfo()` to parse the `AndroidManifest.xml` from the copied APK and read the `<meta-data>` tags. The `package` attribute of the manifest is used as the extension's unique ID.
4.  Store this manifest information in its database.
5.  If the extension is enabled, use a `DexClassLoader` to load the specified `className` from the stored APK file.
6.  Instantiate the loaded class and interact with it through the defined API interfaces (e.g., `MovieProviderExtension`).

## API Versioning

-   The host application defines `CURRENT_HOST_EXTENSION_API_VERSION`.
-   Each extension declares its targeted `EXTENSION_API_VERSION` in its manifest.
-   The host app may refuse to load extensions that target an `apiVersion` significantly different (especially newer) than its own `CURRENT_HOST_EXTENSION_API_VERSION` to prevent compatibility issues.

---
*This API is subject to change as Tomato evolves.*
md
File 'docs/api/extension-api.md' overwritten successfully.
