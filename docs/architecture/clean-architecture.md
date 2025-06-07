# Clean Architecture in Tomato

This document details how the principles of Clean Architecture are applied within the Tomato application to promote separation of concerns, testability, and maintainability.

## Core Principles

- Layers (Entities, Use Cases, Interface Adapters, Frameworks & Drivers)
- Dependency Rule
- Boundaries

## Module Structure

- `:app` (Application Layer)
- `:data` (Data Layer)
- `:domain` (Domain Layer)
- `:core:*` (Core Utility Modules)
- `:feature:*` (Feature Modules)

## Data Flow

- UI to ViewModel
- ViewModel to UseCase
- UseCase to Repository
- Repository to Data Sources (Network/Database)

## Benefits in Tomato

- Testability of components
- Scalability of features
- Maintainability over time

## Deviations and Considerations
md
File 'docs/architecture/clean-architecture.md' created successfully.
