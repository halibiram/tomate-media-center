# Dependency Injection Strategy

This document outlines the dependency injection (DI) strategy used in Tomato, focusing on Hilt.

## Overview

- Why Dependency Injection?
- Chosen Framework: Hilt (Dagger)

## Hilt Implementation

- Application Class (`@HiltAndroidApp`)
- Android Entry Points (`@AndroidEntryPoint`)
- ViewModel Injection (`@HiltViewModel`)
- Modules (`@Module`, `@InstallIn`)
- Providers (`@Provides`)
- Qualifiers (`@Qualifier`)
- Scopes (`@Singleton`, `@ActivityScoped`, etc.)

## Key Modules

- `AppModule` (Application-wide bindings)
- `NetworkModule` (Networking components like Ktor client)
- `DatabaseModule` (Room database and DAOs)
- `DataStoreModule` (Preferences DataStore)
- Feature-specific modules (e.g., `HomeModule`, `PlayerModule`)

## Testing with Hilt

- Unit testing ViewModels and repositories
- UI testing with Hilt
- Replacing bindings in tests

## Best Practices

- Constructor Injection
- Field Injection (for Android framework classes)
- Interface-based bindings
md
File 'docs/architecture/dependency-injection.md' created successfully.
