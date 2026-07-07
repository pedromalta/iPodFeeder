package net.pedromalta.ipodfeeder.audio

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.outputStream
import kotlin.io.path.pathString

class DesktopAudioProcessingEngine : AudioProcessingEngine {
	private val httpClient = HttpClient(OkHttp)
	private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
	private val bundledToolResolver = BundledToolResolver()

	override suspend fun process(
		request: AudioProcessingRequest,
		onProgress: (String) -> Unit
	): AudioProcessingResult {
		require(request.youtubeUrl.isNotBlank()) { "YouTube URL is required." }
		log(onProgress, "Starting processing request")
		log(onProgress, "Input URL: ${request.youtubeUrl}")
		log(onProgress, "Output directory: ${request.outputDirectory}")
		log(onProgress, "Add to Music library: ${request.addToMusicLibrary}")

		val outputDir = Path.of(request.outputDirectory)
		withContext(Dispatchers.IO) { Files.createDirectories(outputDir) }
		log(onProgress, "Ensured output directory exists: ${outputDir.pathString}")

		log(onProgress, "Checking dependency availability")
		val ytDlpExecutable = resolveExecutable("yt-dlp", onProgress)
		val ffmpegExecutable = resolveExecutable("ffmpeg", onProgress)
		runCommand(listOf(ytDlpExecutable, "--version"), onProgress, "yt-dlp version check")
		runCommand(listOf(ffmpegExecutable, "-version"), onProgress, "ffmpeg version check")

		val tempDir = withContext(Dispatchers.IO) { Files.createTempDirectory("ipodfeeder-") }
		log(onProgress, "Created temporary workspace: ${tempDir.pathString}")
		try {
			log(onProgress, "Fetching video metadata from yt-dlp")
			val metadataOutput = runCommand(
				listOf(
					ytDlpExecutable,
					"--skip-download",
					"--no-warnings",
					"--print",
					"%(title)s\t%(uploader)s\t%(thumbnail)s",
					request.youtubeUrl
				),
				onProgress,
				"yt-dlp metadata"
			)
			val metadataLine = metadataOutput.lineSequence().firstOrNull { it.isNotBlank() }
				?: error("Could not read metadata from yt-dlp.")
			val metadata = parseYtDlpMetadata(metadataLine)
			log(onProgress, "Parsed metadata -> title='${metadata.title}', artist='${metadata.artist}'")
			log(onProgress, "Thumbnail URL present: ${metadata.thumbnailUrl != null}")

			log(onProgress, "Downloading best audio stream")
			val sourceTemplate = tempDir.resolve("source.%(ext)s").toString()
			log(onProgress, "yt-dlp output template: $sourceTemplate")
			runCommand(
				listOf(
					ytDlpExecutable,
					"--no-playlist",
					"-f",
					"bestaudio",
					"-o",
					sourceTemplate,
					request.youtubeUrl
				),
				onProgress,
				"yt-dlp audio download"
			)

			val sourceAudio = withContext(Dispatchers.IO) {
				tempDir.listDirectoryEntries()
					.firstOrNull { it.name.startsWith("source.") && it.isRegularFile() }
			} ?: error("Downloaded audio file not found.")
			log(onProgress, "Downloaded source file: ${sourceAudio.pathString} (${sourceAudio.readableSize()})")

			val coverPath = metadata.thumbnailUrl?.let { thumbnailUrl ->
				log(onProgress, "Downloading album art from thumbnail URL")
				downloadCover(tempDir, thumbnailUrl, onProgress)
			}
			if (coverPath != null) {
				log(onProgress, "Album art saved at: ${coverPath.pathString} (${coverPath.readableSize()})")
			} else {
				log(onProgress, "Album art unavailable, continuing without cover art")
			}

			val outputFileName = sanitizeFileSegment("${metadata.artist} - ${metadata.title}") + ".mp3"
			val outputFile = outputDir.resolve(outputFileName)
			log(onProgress, "Target MP3 file: ${outputFile.pathString}")

			log(onProgress, "Converting to MP3 and embedding ID3 tags")
			val ffmpegCommand = buildFfmpegCommand(ffmpegExecutable, sourceAudio, coverPath, outputFile, metadata)
			log(onProgress, "Prepared ffmpeg command with ${ffmpegCommand.size} arguments")
			runCommand(ffmpegCommand, onProgress, "ffmpeg transcode")
			log(onProgress, "MP3 created at ${outputFile.pathString} (${outputFile.readableSize()})")

			val addedToMusic = if (request.addToMusicLibrary) {
				log(onProgress, "Adding track to Music library through AppleScript")
				importToMusicLibrary(outputFile, onProgress)
				log(onProgress, "Music library import succeeded")
				true
			} else {
				log(onProgress, "Music library import skipped by user preference")
				false
			}

			log(onProgress, "Pipeline completed successfully")
			return AudioProcessingResult(
				outputFilePath = outputFile.toString(),
				title = metadata.title,
				artist = metadata.artist,
				addedToMusicLibrary = addedToMusic
			)
		} finally {
			cleanupDirectory(tempDir, onProgress)
		}
	}

	private fun buildFfmpegCommand(
		ffmpegExecutable: String,
		sourceAudio: Path,
		coverPath: Path?,
		outputFile: Path,
		metadata: TrackMetadata
	): List<String> {
		return buildList {
			add(ffmpegExecutable)
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

	private fun resolveExecutable(
		toolName: String,
		onProgress: (String) -> Unit
	): String {
		val bundledTool = bundledToolResolver.resolve(toolName)
		if (bundledTool != null) {
			log(onProgress, "Using bundled $toolName from ${bundledTool.path.pathString}")
			return bundledTool.path.toString()
		}

		log(onProgress, "Bundled $toolName not found, falling back to PATH")
		return toolName
	}

	private suspend fun runCommand(
		args: List<String>,
		onProgress: (String) -> Unit,
		label: String
	): String = withContext(Dispatchers.IO) {
		val commandString = args.joinToString(" ")
		log(onProgress, "[$label] Executing command: $commandString")
		val startTime = System.currentTimeMillis()
		val process = ProcessBuilder(args)
			.redirectErrorStream(true)
			.start()

		val output = process.inputStream.bufferedReader().readText().trim()
		val exitCode = process.waitFor()
		val elapsedMs = System.currentTimeMillis() - startTime
		log(onProgress, "[$label] Exit code: $exitCode (${elapsedMs}ms)")
		if (output.isNotBlank()) {
			log(onProgress, "[$label] Output: ${output.summarizeForLog()}")
		}
		if (exitCode != 0) {
			error("Command failed: $commandString\n$output")
		}
		output
	}

	private suspend fun importToMusicLibrary(mp3Path: Path, onProgress: (String) -> Unit) {
		val escapedPath = mp3Path.toString().replace("\\", "\\\\").replace("\"", "\\\"")
		val script = "tell application \"Music\" to add POSIX file \"$escapedPath\""
		runCommand(listOf("osascript", "-e", script), onProgress, "osascript import")
	}

	private suspend fun downloadCover(
		tempDir: Path,
		thumbnailUrl: String,
		onProgress: (String) -> Unit
	): Path? = withContext(Dispatchers.IO) {
		val bytes = runCatching {
			httpClient.get(thumbnailUrl).body<ByteArray>()
		}.onFailure {
			log(onProgress, "Failed to download album art: ${it.message}")
		}.getOrNull() ?: return@withContext null

		val coverPath = tempDir.resolve("cover.jpg")
		coverPath.outputStream().use { it.write(bytes) }
		log(onProgress, "Album art download size: ${bytes.size} bytes")
		if (coverPath.exists()) coverPath else null
	}

	private suspend fun cleanupDirectory(directory: Path, onProgress: (String) -> Unit) = withContext(Dispatchers.IO) {
		if (!directory.exists()) {
			log(onProgress, "Cleanup skipped, temp directory already removed")
			return@withContext
		}
		var deletedCount = 0
		Files.walk(directory)
			.sorted(Comparator.reverseOrder())
			.forEach {
				if (it.deleteIfExists()) {
					deletedCount += 1
				}
			}
		log(onProgress, "Cleaned temp workspace '${directory.pathString}' (deleted $deletedCount entries)")
	}

	private fun log(onProgress: (String) -> Unit, message: String) {
		onProgress("[${LocalTime.now().format(timeFormatter)}] $message")
	}

	private fun String.summarizeForLog(maxLength: Int = 320): String {
		val singleLine = replace("\n", " | ")
		return if (singleLine.length <= maxLength) singleLine else singleLine.take(maxLength) + "..."
	}

	private fun Path.readableSize(): String {
		val size = runCatching { Files.size(this) }.getOrDefault(0L)
		return if (size < 1024) "$size B" else "${size / 1024} KB"
	}
}
