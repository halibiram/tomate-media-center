# Building Tomato

This document explains how to build the Tomato application for different purposes.

## Build Types

- **Debug:** For development and testing. Includes debug symbols and is typically not minified.
  - Command: `./gradlew assembleDebug`
- **Release:** For production. Signed, minified, and optimized.
  - Command: `./gradlew assembleRelease` (Requires signing configuration)

## App Bundles (AAB) vs APKs

- Explanation of AAB and its benefits for Google Play.
- Building an App Bundle: `./gradlew bundleRelease`
- Building a universal APK from an AAB (for testing or specific distribution): `bundletool build-apks ...`

## Signing Release Builds

- Keystore setup (location, alias, passwords).
- Configuring `signingConfigs` in `build.gradle`.
- Using environment variables or `local.properties` for secure handling of credentials in CI/CD.

## Build Variants & Flavors (If applicable)

- Explanation of any product flavors (e.g., free/premium, different branding).
- How to build specific variants (e.g., `./gradlew assembleDemoDebug`).

## Output Locations

- APKs: `app/build/outputs/apk/`
- AABs: `app/build/outputs/bundle/`
- Mapping files (for Proguard/R8): `app/build/outputs/mapping/`

## Cleaning the Build

- Command: `./gradlew clean`
md
File 'docs/development/building.md' created successfully.
