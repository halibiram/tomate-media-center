# Tomato Features

This document highlights the key features of the Tomato application, providing a user-centric overview.

## 🏠 Home Screen

- **Discover Content:** Easily browse sections like "Popular Movies," "Trending Today," and content provided by your installed "Extensions."
- **Dynamic Carousels:** Content is presented in horizontally scrolling lists for quick visual access.
- **Quick Navigation:** Tap on any movie or item to view its details (feature in progress).

## 🔍 Search

- **Unified Search:** Find movies from your local library (if applicable) and from installed content extensions using a single search bar.
- **Instant Results:** Search results appear as you type (with debouncing for performance).
- **Clear Categorization:** Results from different sources (e.g., "Library," "Extensions") are clearly delineated.

## ▶️ Advanced Media Player

- **Robust Playback:** Powered by `androidx.media3` (ExoPlayer) for reliable playback of various formats.
- **Intuitive Gesture Controls:**
    - Single tap to toggle player controls visibility.
    - Double tap on screen sides to seek forward or backward.
    - (Future: Swipe for brightness/volume).
- **Comprehensive Controls:** On-screen controls for play/pause, seek bar, time display, fullscreen toggle, and settings.
- **Background Playback:** Continue listening to audio when the app is in the background or screen is off, with notification controls.
- **Google Cast Support:** Stream your media to Chromecast and other Google Cast-enabled devices.
- **Subtitle & Audio Track Selection:**
    - Automatically discovers available subtitle and audio tracks.
    - Easily switch between different language subtitles or audio tracks via a selection dialog in player settings.
    - Option to disable subtitles.

## 📥 Downloads

- **Offline Viewing:** Download movies and other media content to watch later without an internet connection.
- **Background Management:** Downloads continue in the background, managed by `WorkManager`.
- **Progress Tracking:** View download progress (percentage, size) and status (Downloading, Paused, Completed, Failed) in a dedicated "Downloads" screen.
- **Download Controls:** Pause, resume, cancel, or retry failed downloads. Delete completed downloads to free up space.
- **Notifications:** Stay informed about download progress and completion via system notifications.

## ⭐ Bookmarks

- **Save Your Favorites:** Easily bookmark movies and series you want to watch later or keep track of.
- **Centralized Access:** View all your bookmarked items in a dedicated "Bookmarks" screen.
- **Filter:** Quickly filter bookmarks by type (e.g., "Movies," "Series") to find what you're looking for.
- **Easy Management:** Add or remove bookmarks with a single tap from media detail screens or the bookmarks list.

## ⚙️ Settings & Customization

- **Appearance:**
    - Choose between Light, Dark, or System Default app themes.
    - (Future: Select accent colors or further customize the look).
- **Player Preferences:**
    - Set default subtitle language.
    - Configure auto-play for next episodes.
    - Adjust seek increment duration.
    - Set preferred playback speed.
    - (Future: Set preferred video resolution).
- **Data Management:**
    - Enable Data Saver mode to reduce data usage on mobile networks.
    - (Future: Manage cache, sync settings).
- **Account:**
    - (Future: Manage user profile, login/logout if account system is implemented).

## 🧩 Extensions System

- **Expand Your Content:** Install third-party extensions (currently via APK sideloading) to access a wider range of media content and features.
- **Content Integration:** Content from enabled extensions (e.g., popular movies, search results) is integrated directly into the Home and Search screens.
- **Manage Extensions:** View installed extensions, enable/disable them, and uninstall them from the "Manage Extensions" screen.
- **Error Handling:** The app provides feedback if an extension fails to load or encounters an error while fetching content.

---
*This feature list will evolve as the Tomato application develops.*
