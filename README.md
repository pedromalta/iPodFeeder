# iPod Feeder

Desktop Kotlin Multiplatform app that downloads YouTube audio as MP3 and prepares files for importing into macOS Music.

## Requirements

- macOS
- `yt-dlp` installed
- `ffmpeg` installed

## Install the app on macOS

### Option 1: Install from the DMG

1. Open the generated DMG.
2. Drag `iPodFeeder.app` into `Applications`.
3. Launch `iPodFeeder` from `Applications`.

If macOS warns that the app is from an unidentified developer, right-click the app and choose **Open** the first time.

Install runtime dependencies first:

```bash
brew install yt-dlp ffmpeg
```

If conversion fails after launching the app from Finder, verify the tools are available from a normal shell:

```bash
yt-dlp --version
ffmpeg -version
```

### Option 2: Run from source

For local development, you can run directly with Gradle:

```bash
cd /Users/pedromalta/AndroidStudioProjects/iPodFeeder
./gradlew :app:run
```

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

1. Install `yt-dlp` and `ffmpeg`
2. Open the DMG
3. Drag `iPodFeeder.app` to `Applications`
4. Launch the app

## Development

```bash
cd /Users/pedromalta/AndroidStudioProjects/iPodFeeder
./gradlew :app:run
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
4. App runs `yt-dlp` + `ffmpeg`, tags title/artist, and embeds thumbnail when available.

