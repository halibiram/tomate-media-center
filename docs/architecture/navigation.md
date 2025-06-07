# Navigation Architecture

This document describes the navigation strategy implemented in Tomato using Jetpack Compose Navigation.

## Core Concepts

- `NavController`
- `NavHost`
- Navigation Graphs (Nested Graphs)
- Routes (String-based)
- Arguments
- Deep Linking

## Implementation Details

- App-level `NavHost` in `MainActivity`.
- Feature-level navigation graphs (e.g., `homeGraph`, `settingsGraph`).
- Navigation encapsulated within feature modules (e.g., `HomeNavigation.kt`).
- Passing arguments between composables.
- Type-safe argument passing (if applicable, e.g., using helper classes or libraries).

## Global Navigation Actions

- Actions accessible from anywhere (e.g., navigate to Settings).
- Handling back navigation and up navigation.

## Deeplinks

- Structure of deep links.
- Handling deep links in `MainActivity` and routing to appropriate destinations.

## Testing Navigation

- Using `TestNavHostController`.
- Verifying navigation actions and back stack.

## Future Considerations

- Conditional Navigation
- Custom Transitions
md
File 'docs/architecture/navigation.md' created successfully.
