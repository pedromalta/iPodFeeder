package net.pedromalta.ipodfeeder.audio

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.outputStream

class DesktopAudioProcessingEngine : AudioProcessingEngine {
	private val httpClient = HttpClient(OkHttp)

	override suspend fun process(
		request: AudioProcessingRequest,
		onProgress: (String) -> Unit
	): AudioProcessingResult {
		require(request.youtubeUrl.isNotBlank()) { "YouTube URL is required." }

		val outputDir = Path.of(request.outputDirectory)
		withContext(Dispatchers.IO) { Files.createDirectories(outputDir) }

		onProgress("Checking yt-dlp and ffmpeg availability...")
		runCommand(listOf("yt-dlp", "--version"))
		runCommand(listOf("ffmpeg", "-version"))

		val tempDir = withContext(Dispatchers.IO) { Files.createTempDirectory("ipodfeeder-") }
		try {
			onProgress("Fetching video metadata...")
			val metadataOutput = runCommand(
				listOf(
					"yt-dlp",
					"--skip-download",
					"--no-warnings",
					"--print",
					"%(title)s\t%(uploader)s\t%(thumbnail)s",
					request.youtubeUrl
				)
			)
			val metadataLine = metadataOutput.lineSequence().firstOrNull { it.isNotBlank() }
				?: error("Could not read metadata from yt-dlp.")
			val metadata = parseYtDlpMetadata(metadataLine)

			onProgress("Downloading best audio stream...")
			val sourceTemplate = tempDir.resolve("source.%(ext)s").toString()
			runCommand(
				listOf(
					"yt-dlp",
					"--no-playlist",
					"-f",
					"bestaudio",
					"-o",
					sourceTemplate,
					request.youtubeUrl
				)
			)

			val sourceAudio = withContext(Dispatchers.IO) {
				tempDir.listDirectoryEntries()
					.firstOrNull { it.name.startsWith("source.") && it.isRegularFile() }
			} ?: error("Downloaded audio file not found.")

			val coverPath = metadata.thumbnailUrl?.let { thumbnailUrl ->
				onProgress("Downloading album art...")
				downloadCover(tempDir, thumbnailUrl)
			}

			val outputFileName = sanitizeFileSegment("${metadata.artist} - ${metadata.title}") + ".mp3"
			val outputFile = outputDir.resolve(outputFileName)

			onProgress("Converting to MP3 and embedding ID3 tags...")
			runCommand(buildFfmpegCommand(sourceAudio, coverPath, outputFile, metadata))

			val addedToMusic = if (request.addToMusicLibrary) {
				onProgress("Adding track to Music library...")
				importToMusicLibrary(outputFile)
				true
			} else {
				false
			}

			onProgress("Done.")
			return AudioProcessingResult(
				outputFilePath = outputFile.toString(),
				title = metadata.title,
				artist = metadata.artist,
				addedToMusicLibrary = addedToMusic
			)
		} finally {
			cleanupDirectory(tempDir)
		}
	}

	private fun buildFfmpegCommand(
		sourceAudio: Path,
		coverPath: Path?,
		outputFile: Path,
		metadata: TrackMetadata
	): List<String> {
		return buildList {
			add("ffmpeg")
			add("-y")
			add("-i")
			add(sourceAudio.toString())
			if (coverPath != null) {
				addAll(
					listOf(
						"-i", coverPath.toString(),
						"-map", "0:a:0",
						"-map", "1:v:0",
						"-c:v", "mjpeg",
						"-disposition:v", "attached_pic"
					)
				)
			} else {
				addAll(listOf("-map", "0:a:0"))
			}
			addAll(
				listOf(
					"-c:a", "libmp3lame",
					"-b:a", "320k",
					"-id3v2_version", "3",
					"-metadata", "title=${metadata.title}",
					"-metadata", "artist=${metadata.artist}",
					"-metadata", "album=YouTube",
					outputFile.toString()
				)
			)
		}
	}

	private suspend fun runCommand(args: List<String>): String = withContext(Dispatchers.IO) {
		val process = ProcessBuilder(args)
			.redirectErrorStream(true)
			.start()

		val output = process.inputStream.bufferedReader().readText().trim()
		val exitCode = process.waitFor()
		if (exitCode != 0) {
			error("Command failed: ${args.joinToString(" ")}\n$output")
		}
		output
	}

	private suspend fun importToMusicLibrary(mp3Path: Path) {
		val escapedPath = mp3Path.toString().replace("\\", "\\\\").replace("\"", "\\\"")
		val script = "tell application \"Music\" to add POSIX file \"$escapedPath\""
		runCommand(listOf("osascript", "-e", script))
	}

	private suspend fun downloadCover(tempDir: Path, thumbnailUrl: String): Path? = withContext(Dispatchers.IO) {
		val bytes = runCatching {
			httpClient.get(thumbnailUrl).body<ByteArray>()
		}.getOrNull() ?: return@withContext null

		val coverPath = tempDir.resolve("cover.jpg")
		coverPath.outputStream().use { it.write(bytes) }
		if (coverPath.exists()) coverPath else null
	}

	private suspend fun cleanupDirectory(directory: Path) = withContext(Dispatchers.IO) {
		if (!directory.exists()) return@withContext
		Files.walk(directory)
			.sorted(Comparator.reverseOrder())
			.forEach { it.deleteIfExists() }
	}
}

