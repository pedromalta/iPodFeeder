package net.pedromalta.ipodfeeder.audio

fun parseYtDlpMetadata(line: String): TrackMetadata {
	val parts = line.trim().split("\t", limit = 3)
	val title = parts.getOrNull(0)?.trim().orEmpty().ifBlank { "Unknown Title" }
	val artist = parts.getOrNull(1)?.trim().orEmpty().ifBlank { "Unknown Artist" }
	val thumbnail = parts.getOrNull(2)?.trim().orEmpty().ifBlank { null }
	return TrackMetadata(title = title, artist = artist, thumbnailUrl = thumbnail)
}

