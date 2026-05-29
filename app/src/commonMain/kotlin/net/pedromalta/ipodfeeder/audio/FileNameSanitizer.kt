package net.pedromalta.ipodfeeder.audio

private val forbiddenChars = Regex("[\\\\/:*?\"<>|]")
private val whitespace = Regex("\\s+")

fun sanitizeFileSegment(input: String): String {
	val stripped = input.replace(forbiddenChars, " ")
	val squashed = stripped.replace(whitespace, " ").trim()
	return squashed.ifBlank { "track" }
}

