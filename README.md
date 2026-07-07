# iPod Feeder

Desktop Kotlin Multiplatform app that downloads YouTube audio as MP3 and prepares files for importing into macOS Music.

## Requirements

- macOS
- Java 17 for local Gradle builds and packaging

## Install the app on macOS

### Option 1: Install from the DMG

1. Open the generated DMG.
2. Drag `iPodFeeder.app` into `Applications`.
3. Launch `iPodFeeder` from `Applications`.

If macOS warns that the app is from an unidentified developer, right-click the app and choose **Open** the first time.

The packaged app now bundles `yt-dlp` and `ffmpeg` for the macOS architecture it was built on, so end users do not need to install them separately.

### Option 2: Run from source

For local development, you can run directly with Gradle:

```bash
cd /Users/pedromalta/AndroidStudioProjects/iPodFeeder
./gradlew :app:run
```

The first Gradle run downloads pinned `yt-dlp` and `ffmpeg` binaries and embeds them into the desktop app resources.

## Build a DMG for distribution

This project is currently configured to package the macOS app with Zulu 17:

- `/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home`

Build the DMG with:

```bash
cd /Users/pedromalta/AndroidStudioProjects/iPodFeeder
./gradlew :app:packageDistributionForCurrentOS
```

Generated output:

```text
app/build/compose/binaries/main/dmg/iPodFeeder-1.0.0.dmg
```

You can open the generated DMG with:

```bash
open "/Users/pedromalta/AndroidStudioProjects/iPodFeeder/app/build/compose/binaries/main/dmg/iPodFeeder-1.0.0.dmg"
```

## Distribute to other people

Share the generated `.dmg` file.

Recipients should:

1. Open the DMG
2. Drag `iPodFeeder.app` to `Applications`
3. Launch the app

## Development

```bash
cd /Users/pedromalta/AndroidStudioProjects/iPodFeeder
./gradlew :app:run
./gradlew :app:packageDistributionForCurrentOS
```

## Test

```bash
cd /Users/pedromalta/AndroidStudioProjects/iPodFeeder
./gradlew :app:desktopTest
```

## Current Flow

1. Paste a YouTube URL.
2. Choose output directory.
3. Click **Convert to MP3**.
4. App runs bundled `yt-dlp` + `ffmpeg`, tags title/artist, and embeds thumbnail when available.
