# Project Plan

iPod Feeder - A KMP app for macOS to download YouTube videos as MP3 and add them to the macOS Music library. This project starts from an existing Android project and needs to be migrated/setup for KMP macOS.

## Project Brief

# iPod Feeder - Project Brief (macOS MVP)

**iPod Feeder** is a desktop utility built with Kotlin Multiplatform (KMP) designed to streamline the process of moving music from YouTube to the macOS Music library. It pivots from a mobile-first approach to a dedicated desktop experience, focusing on seamless OS-level integration.

### Features
*   **YouTube to MP3 Conversion**: High-fidelity audio extraction from YouTube URLs directly on macOS.
*   **macOS Music Integration**: Automatic injection of downloaded MP3s into the macOS Music app library using system scripting.
*   **Automatic ID3 Tagging**: Automatic retrieval and embedding of metadata (Artist, Title, and Album Art) into files for a clean library organization.
*   **Desktop-Optimized Interface**: A native-feeling macOS UI built with Compose Multiplatform, supporting system dark mode and window management.

### High-Level Technical Stack
*   **Kotlin Multiplatform (KMP) & Compose Multiplatform**: To transition the current Android project into a dedicated macOS desktop application.
*   **Kotlin Coroutines**: For non-blocking audio conversion and download processes.
*   **KSP (Kotlin Symbol Processing)**: Optimized code generation for any multiplatform dependencies.
*   **Ktor**: For efficient multiplatform network requests and large file streaming.
*   **AppleScript / JXA Integration**: Used via system execution to programmatically add files and playlists to the macOS Music app.
*   **FFmpeg (Bundled)**: Utilized as a native binary to handle high-quality audio transcoding.

## Implementation Steps

### Task_1_KMP_Migration: Migrate the existing Android project to a Kotlin Multiplatform (KMP) project targeting macOS. Update gradle configurations, setup common and macOS source sets, and integrate necessary KMP libraries (Ktor, Coroutines, etc.).
- **Status:** DONE
- **Acceptance Criteria:**
  - Project successfully builds for macOS target.
  - Common source set and macOS source set are correctly configured.
  - Basic Compose Multiplatform 'Hello World' runs on macOS.
- **StartTime:** 2026-05-29 14:32:24 BRT

### Task_2_Audio_Processing_Engine: Implement the core logic for downloading YouTube videos, transcoding them to MP3 using FFmpeg, and embedding ID3 metadata (Artist, Title, Album Art).
- **Status:** DONE
- **Acceptance Criteria:**
  - YouTube audio extraction logic is functional.
  - FFmpeg is integrated and successfully converts downloads to MP3.
  - Downloaded files have correct ID3 tags and artwork embedded.

### Task_3_macOS_Integration_and_UI: Develop the macOS-specific Compose Multiplatform UI following Material 3 guidelines and implement the AppleScript bridge to add downloaded tracks to the macOS Music library.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - User interface allows URL input and displays progress.
  - Material 3 theme and vibrant color scheme applied.
  - AppleScript integration successfully adds files to the macOS Music app.

### Task_4_Final_Verification: Perform a full end-to-end test of the application to ensure stability, verify all requirements are met, and ensure no crashes occur during the download or integration process.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Full flow (URL -> MP3 -> Music Library) works seamlessly.
  - App does not crash under normal operation.
  - Build passes and all existing tests pass.
  - UI is verified for Material 3 alignment and responsiveness.

