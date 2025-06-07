# Troubleshooting Guide

This guide provides solutions to common issues you might encounter while using Tomato.

## Playback Issues

- **Video Not Playing / Black Screen:**
    - Check if the file format is supported.
    - Ensure your device's hardware decoders are functioning (try toggling hardware/software decoding in settings if available).
    - The file might be corrupted.
    - For online streams (via extensions), check your internet connection.
- **No Audio:**
    - Check device volume and app volume (if separate).
    - Try selecting a different audio track if available.
    - The audio codec might not be supported (rare for common formats).
- **Subtitles Not Displaying:**
    - Ensure subtitles are enabled in player settings.
    - Check if the subtitle file is correctly named and in the same folder as the video (for external subs).
    - Verify the subtitle format is supported (e.g., SRT, ASS).
- **Buffering or Lagging Playback:**
    - For local files: Your device might be underpowered for very high-bitrate files.
    - For network/extension streams: Check your internet speed and Wi-Fi connection. Try a lower quality if available.

## Library Issues

- **Media Not Appearing in Library:**
    - Ensure the correct folders are added in Library settings.
    - Perform a library rescan.
    - Check file naming conventions, especially for TV shows (e.g., `ShowName S01E01.mkv`).
- **Incorrect Metadata (Poster, Summary, etc.):**
    - Try refreshing metadata for the specific item.
    - Ensure your device is connected to the internet for metadata fetching.
    - The item might be ambiguously named; try correcting the file name or manually identifying it.

## Extension Issues

- **Extension Not Loading/Working:**
    - Ensure the extension is enabled in the Extensions management screen.
    - Check if the extension is compatible with your version of Tomato (API version).
    - The extension might require specific permissions or setup.
    - Look for error messages from the extension itself.
- **Content from Extension Not Appearing:**
    - Verify the extension is configured correctly (if it requires login or API keys).
    - Check your internet connection.

## Download Issues

- **Download Stuck or Failing:**
    - Check available storage space.
    - Verify your internet connection.
    - The source URL might be invalid or expired.
    - Try pausing and resuming the download.
    - Ensure the app has necessary permissions (e.g., storage, background activity).

## General Issues

- **App Crashing or Freezing:**
    - Try clearing the app cache (Settings > Apps > Tomato > Storage > Clear Cache).
    - Ensure your app is updated to the latest version.
    - If the issue persists, try reinstalling the app (backup any important data if possible).
    - Report the bug with details (see [Contributing Guide](../development/contributing.md)).
- **Settings Not Saving:**
    - Ensure the app has permissions to write to storage if settings are file-based.
    - This could be a bug; please report it.

## Getting Help

- If your issue is not listed here, please check the [GitHub Issues](https://github.com/your-repo/tomato/issues) page.
- You can open a new issue if your problem hasn't been reported. Provide as much detail as possible (app version, device model, Android version, steps to reproduce, logs if available).
md
File 'docs/user-guide/troubleshooting.md' created successfully.
