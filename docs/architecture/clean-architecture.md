# Clean Architecture in Tomato

This document details how the principles of Clean Architecture are applied within the Tomato application to promote separation of concerns, testability, and maintainability, leading to a more robust and scalable codebase.

## Core Principles Adhered To

- **Layers:** The application is structured into distinct layers, primarily Presentation, Domain, and Data.
    - **Entities (Domain Models):** Represent core business objects (e.g., `Movie`, `Series`, `Download`, `Bookmark`, `Extension`, `User`). These are simple Kotlin data classes and are part of the Domain layer.
    - **Use Cases (Interactors):** Encapsulate specific application business logic (e.g., `GetMoviesUseCase`, `DownloadMediaUseCase`). They are part of the Domain layer and orchestrate data flow between Presentation and Data layers.
    - **Interface Adapters (ViewModel/Repository Interfaces):** ViewModels in the Presentation layer act as adapters for the UI, and Repository Interfaces in the Domain layer define contracts for data access.
    - **Frameworks & Drivers (UI, Database, Network, Services):** The outer layer, including Jetpack Compose UI, Room database, Ktor/Retrofit for networking, Android Services, etc.
- **Dependency Rule:** Dependencies point inwards. The UI depends on ViewModels, ViewModels on UseCases, UseCases on Repository Interfaces. The Data layer implements Repository Interfaces and depends on database/network frameworks. The Domain layer has no dependencies on outer layers (Data, Presentation, or specific Android frameworks).
- **Boundaries:** Clear boundaries are maintained between layers using interfaces (Repository interfaces defined in Domain, implemented in Data) and domain models.

## Module Organization

The project is organized into the following Gradle modules to enforce separation and improve build times:

-   **`:app` (Application Layer):**
    -   The main application module.
    -   Contains `MainActivity`, top-level UI navigation graph (`NavHost`), and DI setup (`TomatoApplication` class, Hilt wiring).
    -   Coordinates feature modules and UI components from `:core:ui`.
-   **`:core:*` (Core Utility Modules):**
    -   **`:core:common`**: Contains common utilities, base classes, and shared models like `Result.kt`, `TomatoException.kt`.
    -   **`:core:network`**: Networking setup (Ktor client, `NetworkModule` for DI), API interface definitions (e.g., `MovieApi`), DTOs, and Mappers (DTO to Domain). *Note: API interfaces and DTOs/Mappers are currently in `:data`, but could be in `:core:network` if shared by multiple data sources.* For now, assume they are in `:data`.
    -   **`:core:database`**: Room database setup (`TomatoDatabase`, DAOs, Entities, `DatabaseModule` for DI), type converters.
    -   **`:core:datastore`**: Jetpack DataStore Preferences setup (`UserPreferences`, `AppPreferences`, `PlayerPreferences`, `DataStoreModule` for DI).
    -   **`:core:player`**: Media playback logic using `androidx.media3` (ExoPlayer), including `PlayerManager`, `TomatoExoPlayer`, `MediaSourceFactory`, `PlayerService` for background playback, `PlayerNotificationManager`, and Cast integration (`CastManager`, `CastOptionsProvider`).
    -   **`:core:ui`**: (If created) Shared Jetpack Compose components, themes, styles used across multiple features (e.g., `TomatoCard`, `TomatoTopBar` currently in `:app`).
-   **`:domain` (Domain Layer):**
    -   Contains core business logic and is independent of Android frameworks.
    -   **Domain Models:** (e.g., `Movie`, `Series`, `Episode`, `Download`, `Bookmark`, `Extension`, `User`).
    -   **Repository Interfaces:** Defines contracts for data operations (e.g., `MovieRepository`, `DownloadRepository`).
    -   **Use Cases (Interactors):** Encapsulates specific pieces of business logic (e.g., `GetMoviesUseCase`, `InstallExtensionUseCase`).
-   **`:data` (Data Layer):**
    -   Implements repository interfaces defined in the `:domain` layer (e.g., `MovieRepositoryImpl`).
    -   Manages data sources:
        -   **Remote:** Network API calls using Ktor/Retrofit (via API interfaces defined in this layer or `:core:network`). Contains DTOs and mappers to/from domain models.
        -   **Local:** Database operations via DAOs (from `:core:database`), DataStore preferences (from `:core:datastore`).
-   **`:feature:*` (Feature Modules):**
    -   Self-contained features like `:feature:home`, `:feature:search`, `:feature:player`, `:feature:downloads`, `:feature:bookmarks`, `:feature:settings`, `:feature:extensions`.
    -   Each typically contains its own UI (Compose screens/components), ViewModels, and feature-specific DI modules (if needed).
    -   Depends on `:domain` for use cases and `:core` modules for shared functionality.
-   **`extensions/dummy_success_provider` (Example Extension Module):**
    -   An example of an external extension, built as a separate APK.
    -   Implements interfaces from `:feature:extensions:api` (or a dedicated `:extension-api` module).

## Data Flow Example (Home Screen Movies)

1.  **UI (`HomeScreen.kt`):** Observes `HomeUiState` from `HomeViewModel`. User interaction (e.g., retry) calls ViewModel methods.
2.  **ViewModel (`HomeViewModel.kt`):**
    -   Injected with `GetMoviesUseCase` and `GetExtensionMoviesUseCase`.
    -   Requests data by calling the use cases.
    -   Collects `Flow<Result<List<Movie/MovieSourceItem>>>` from use cases.
    -   Updates `HomeUiState` (isLoading, data list, error) for the UI to consume.
3.  **UseCase (`GetMoviesUseCase.kt`, `GetExtensionMoviesUseCase.kt`):**
    -   Injected with `MovieRepository` or `ExtensionEngine`.
    -   Calls appropriate methods on the repository/engine (e.g., `movieRepository.getPopularMovies()`, `extensionEngine.getAllPopularMovies()`).
    -   May perform minor business logic or data combination.
4.  **Repository (`MovieRepositoryImpl.kt`, `ExtensionEngine.kt`):**
    -   `MovieRepositoryImpl`: Injected with `MovieApi` (remote) and `MovieDao` (local).
        -   Fetches data from `MovieApi` (DTOs).
        -   Maps DTOs to Domain Models.
        -   Saves domain models as Entities to `MovieDao`.
        -   Emits `Result` with Domain Models (potentially from DAO first, then network).
    -   `ExtensionEngine`: Injected with `ExtensionLoader`.
        -   Manages loaded extension instances.
        -   Calls methods on `MovieProviderExtension` instances.
        -   Returns data (e.g., `Map<String, Result<List<MovieSourceItem>>>`).
5.  **Data Sources:**
    -   **Remote (`MovieApi` impl):** Makes HTTP calls using Ktor to an external API.
    -   **Local (`MovieDao`):** Interacts with the Room database.
    -   **Extensions (`MovieProviderExtension` impl in APK):** Provides data from its own source.

## Dependency Rule Enforcement

-   The Domain layer is pure Kotlin and has no dependencies on Android framework classes or other layers like Data or Presentation.
-   Presentation (features, app) and Data layers depend on Domain (for models, use cases, repository interfaces).
-   Core modules provide specific functionalities (networking, database, UI components) and are depended upon by feature, data, or app modules as needed.
-   Hilt is used for Dependency Injection, helping manage dependencies between layers and modules.

## Benefits in Tomato

-   **Testability:** Clear separation allows for easier unit testing of UseCases, ViewModels, and Repositories by mocking dependencies.
-   **Maintainability:** Changes in one layer (e.g., UI framework, database implementation) have minimal impact on other layers.
-   **Scalability:** New features can be added as independent modules without significantly affecting existing ones.
-   **Developer Parallelization:** Different teams/developers can work on different layers/features simultaneously.

## Deviations and Considerations

-   **Extension API Location:** The `ExtensionAPI.kt` is currently in `feature/extensions`. For true decoupling, it might be better in a dedicated `:extension-api` library module that both the host app and external extensions depend on.
-   **Core UI Module:** Shared UI components are currently in `:app`, but could be moved to a `:core:ui` module for better reusability by features if needed.
-   **Error Granularity:** While `Result` and `TomatoException` are used, error handling granularity (e.g., specific error codes, user-friendly messages from different layers) is an ongoing refinement.
md
File 'docs/architecture/clean-architecture.md' overwritten successfully.
