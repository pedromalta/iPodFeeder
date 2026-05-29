package net.pedromalta.ipodfeeder.audio

interface AudioProcessingEngine {
    suspend fun process(
        request: AudioProcessingRequest,
        onProgress: (String) -> Unit = {}
    ): AudioProcessingResult
}

data class AudioProcessingRequest(
    val youtubeUrl: String,
    val outputDirectory: String,
    val addToMusicLibrary: Boolean = true
)

data class AudioProcessingResult(
    val outputFilePath: String,
    val title: String,
    val artist: String,
    val addedToMusicLibrary: Boolean
)

data class TrackMetadata(
    val title: String,
    val artist: String,
    val thumbnailUrl: String?
)

