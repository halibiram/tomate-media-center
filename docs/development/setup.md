# Development Setup Guide

This guide provides instructions for setting up the development environment for Tomato.

## Prerequisites

- Android Studio (Latest Stable Version Recommended)
- JDK (Version 17 or as specified in `build.gradle`)
- Git

## Getting the Code

- Cloning the repository: `git clone <repository-url>`
- Importing the project into Android Studio.

## Build Configuration

- `local.properties`: (Explain if any specific properties need to be set, e.g., API keys for development builds. Often this file is gitignored.)
- Build Variants (Debug, Release).
- Environment Variables (If any used during build).

## First Build

- Syncing Gradle files.
- Running `./gradlew assembleDebug` from the command line.
- Running the app on an emulator or physical device.

## Common Issues & Troubleshooting

- Gradle sync errors.
- Missing SDK components.
- Emulator setup problems.

## IDE Configuration (Optional)

- Recommended plugins.
- Code style settings.
md
File 'docs/development/setup.md' created successfully.
