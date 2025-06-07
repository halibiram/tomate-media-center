# Testing Guide

This document outlines the testing strategies and practices for the Tomato application.

## Types of Tests

- **Unit Tests:**
  - Location: `module/src/test/kotlin/`
  - Frameworks: JUnit 4/5, Mockito/MockK
  - Purpose: Testing individual classes and functions (ViewModels, UseCases, Repositories, utility classes).
  - Command: `./gradlew testDebugUnitTest` (or for specific modules)
- **Integration Tests (Android Instrumented Tests):**
  - Location: `module/src/androidTest/kotlin/`
  - Frameworks: Espresso, UI Automator, Jetpack Compose Test Rule, AndroidJUnit4
  - Purpose: Testing interactions between components, UI behavior, navigation, database operations, and permission handling.
  - Command: `./gradlew connectedDebugAndroidTest` (Requires an emulator or device)
- **End-to-End (E2E) Tests (If applicable):**
  - Frameworks: (e.g., Appium, custom scripts)
  - Purpose: Testing full application flows from a user's perspective.

## Running Tests

- From Android Studio: (Right-click on test class/method or directory)
- From Command Line: (Using Gradle commands mentioned above)

## Test Coverage

- Tools: JaCoCo (for generating coverage reports)
- Command: `./gradlew jacocoTestReport` (or similar, depends on setup)
- Viewing reports: `module/build/reports/jacoco/`

## Mocking

- Libraries: Mockito (Java-focused) or MockK (Kotlin-idiomatic).
- When to use: Isolating components under test by replacing real dependencies with controlled fakes.

## UI Testing with Jetpack Compose

- `createComposeRule()` / `createAndroidComposeRule()`
- Finding Nodes (`onNodeWithText`, `onNodeWithTag`, etc.)
- Performing Actions (`performClick`, `performScrollTo`, etc.)
- Assertions (`assertIsDisplayed`, `assertTextEquals`, etc.)
- Handling asynchronous operations.

## Best Practices

- Writing clear and maintainable tests.
- Aiming for good test coverage.
- Keeping unit tests fast.
- Using descriptive test method names.
md
File 'docs/development/testing.md' created successfully.
