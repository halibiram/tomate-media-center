# Contributing to Tomato

We welcome contributions to Tomato! This document outlines how to contribute to the project.

## Getting Started

- Ensure you have set up your [Development Environment](./setup.md).
- Familiarize yourself with the project's [Architecture](./../architecture/clean-architecture.md).

## Finding Issues to Work On

- Check the "Issues" tab on GitHub.
- Look for issues tagged with `help wanted` or `good first issue`.
- If you have an idea for a new feature or bug fix, please open an issue first to discuss it.

## Contribution Workflow

1.  **Fork the Repository:** Create your own fork of the main Tomato repository.
2.  **Create a Branch:** Create a new branch from `main` (or `develop` if that's the active development branch) for your changes.
    - Branch naming convention: `feature/your-feature-name` or `fix/issue-number-description`.
3.  **Make Changes:** Implement your feature or bug fix.
    - Follow the project's coding style (e.g., Kotlin coding conventions).
    - Write unit tests for new functionality.
    - Ensure existing tests pass.
4.  **Commit Changes:** Make clear, concise commit messages.
5.  **Push Changes:** Push your branch to your fork.
6.  **Create a Pull Request (PR):**
    - Open a PR from your branch to the main Tomato repository's `main` (or `develop`) branch.
    - Provide a clear description of your changes in the PR.
    - Link any relevant issues.
    - Ensure all CI checks (linting, tests) pass on your PR.
7.  **Code Review:** Project maintainers will review your PR. Address any feedback or requested changes.
8.  **Merge:** Once approved, your PR will be merged.

## Coding Guidelines

- Follow standard Kotlin coding conventions.
- Write clear, readable, and maintainable code.
- Add comments where necessary to explain complex logic.
- Ensure your code is formatted correctly (e.g., using Android Studio's default formatter or a project-specific style).

## Style Guide / Linting

- The project uses Android Lint for static code analysis. Run `./gradlew lintDebug` before submitting a PR.
- Address any lint warnings or errors.

## Testing

- All new features should include unit tests.
- Critical bug fixes should also include tests to prevent regressions.
- Ensure `./gradlew testDebugUnitTest` passes.

## Communication

- Use GitHub Issues for bug reports and feature requests.
- Use GitHub Discussions (if enabled) for general questions and discussions.

Thank you for contributing to Tomato!
md
File 'docs/development/contributing.md' created successfully.
