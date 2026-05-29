package net.pedromalta.ipodfeeder.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AudioHelpersTest {
	@Test
	fun parseYtDlpMetadata_withAllFields() {
		val metadata = parseYtDlpMetadata("Track Name\tArtist Name\thttps://img.example/cover.jpg")

		assertEquals("Track Name", metadata.title)
		assertEquals("Artist Name", metadata.artist)
		assertEquals("https://img.example/cover.jpg", metadata.thumbnailUrl)
	}

	@Test
	fun parseYtDlpMetadata_withMissingThumbnail() {
		val metadata = parseYtDlpMetadata("Track Name\tArtist Name\t")

		assertEquals("Track Name", metadata.title)
		assertEquals("Artist Name", metadata.artist)
		assertNull(metadata.thumbnailUrl)
	}

	@Test
	fun sanitizeFileSegment_replacesForbiddenCharacters() {
		val output = sanitizeFileSegment("A/B:C*D?\"E<F>G|H")

		assertEquals("A B C D E F G H", output)
	}
}

