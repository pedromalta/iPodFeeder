# iPod Feeder

Desktop Kotlin Multiplatform app that downloads YouTube audio as MP3 and prepares files for importing into macOS Music.

## Requirements

- macOS
- JDK 17+
- `yt-dlp` available in `PATH`
- `ffmpeg` available in `PATH`

## Run

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

