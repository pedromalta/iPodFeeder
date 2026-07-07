# iPod Feeder Copilot Instructions

## Build and test commands

From the repository root:

- Run the desktop app: `./gradlew :app:run`
- Build the app and run its configured checks: `./gradlew :app:build`
- Build the macOS distributable DMG: `./gradlew :app:packageDistributionForCurrentOS`
- Run the active automated test suite: `./gradlew :app:desktopTest`
- Run a single test method: `./gradlew :app:desktopTest --tests 'net.pedromalta.ipodfeeder.audio.AudioHelpersTest.parseYtDlpMetadata_withAllFields'`

There is no dedicated lint task configured in this repository today.

## High-level architecture

- This is a single-module Compose Multiplatform app in `:app`, but the only active runtime target today is `jvm("desktop")`. Shared contracts and UI live in `app/src/commonMain`, and desktop-specific implementations live in `app/src/desktopMain`.
- `app/src/commonMain/kotlin/net/pedromalta/ipodfeeder/App.kt` owns the current UI state directly in Compose. It creates the audio engine with `createAudioProcessingEngine()`, launches work with `rememberCoroutineScope`, and streams progress into an in-memory log shown in the UI.
- `app/src/commonMain/kotlin/net/pedromalta/ipodfeeder/audio/AudioProcessingEngine.kt` defines the shared request/result contract. Platform selection happens through `expect/actual` in `AudioProcessingFactory.kt` and `AudioProcessingFactory.desktop.kt`.
- `app/src/desktopMain/kotlin/net/pedromalta/ipodfeeder/audio/DesktopAudioProcessingEngine.kt` is the real conversion pipeline: validate request, create the output directory, resolve bundled tools, fetch metadata with `yt-dlp`, download the best audio stream, optionally download thumbnail art with Ktor, transcode/tag the MP3 with `ffmpeg`, optionally import it into macOS Music with `osascript`, and always clean up the temporary workspace.
- Helper logic that is reused across the pipeline, like yt-dlp metadata parsing and filename sanitization, stays in `commonMain` and is covered by `commonTest`. The legacy `src/test` and `src/androidTest` template tests are not the main path for this project's current desktop target.

## Key conventions

- Desktop builds now pin and bundle `yt-dlp` and `ffmpeg` for the current macOS architecture during Gradle resource processing. Runtime should prefer bundled executables and only fall back to PATH when those resources are unavailable.
- Keep platform-agnostic logic in `commonMain`; keep filesystem, process execution, AppleScript, and OS-default-path behavior in `desktopMain`.
- Pipeline progress is part of the product behavior. When changing the conversion flow, preserve the `onProgress` logging pattern so the UI continues to show step-by-step status and failure details.
- Output files are named from `"artist - title"` and then normalized with `sanitizeFileSegment(...)`. Preserve that flow unless a change explicitly updates file naming behavior.
- The default destination directory is `~/Music/iPod Feeder/Imports`, and Music app import is a separate optional step controlled by `addToMusicLibrary`.
- Packaging is currently pinned to Zulu 17 in `app/build.gradle.kts` via `javaHome = "/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home"`. If packaging changes, keep the Gradle config and README instructions aligned.
