package net.pedromalta.ipodfeeder.audio

fun parseYtDlpMetadata(line: String): TrackMetadata {
	val parts = line.trim().split("\t", limit = 4)
	val title = parts.getOrNull(0)?.trim().orEmpty().ifBlank { "Unknown Title" }
	val artist = parts.getOrNull(1)?.trim().orEmpty().ifBlank { "Unknown Artist" }
	val rawAlbum = parts.getOrNull(2)?.trim().orEmpty()
	val album = rawAlbum.takeUnless { it.equals("NA", ignoreCase = true) }.orEmpty().ifBlank { artist }
	val thumbnail = parts.getOrNull(3)?.trim().orEmpty().ifBlank { null }
	return TrackMetadata(title = title, artist = artist, album = album, thumbnailUrl = thumbnail)
}
