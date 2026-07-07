package net.pedromalta.ipodfeeder.audio

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Properties
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream

internal class BundledToolResolver(
    private val classLoader: ClassLoader = BundledToolResolver::class.java.classLoader,
    private val cacheRoot: Path = defaultBundledToolsCacheRoot(),
    private val platform: BundledToolPlatform? = BundledToolPlatform.currentOrNull()
) {
    fun resolve(toolName: String): ResolvedBundledTool? {
        val normalizedToolName = toolName.toBundledToolName() ?: return null
        val currentPlatform = platform ?: return null
        val manifest = loadManifest(currentPlatform) ?: return null
        val resourcePath = "${currentPlatform.resourceDirectory}/$normalizedToolName"
        val outputDirectory = cacheRoot.resolve(
            "${currentPlatform.directoryName}-${manifest.ffmpegVersion}-${manifest.ytDlpVersion}"
        )
        val outputFile = outputDirectory.resolve(normalizedToolName)

        if (!outputFile.exists() || !outputFile.isRegularFile() || outputFile.fileSize() == 0L) {
            extractResource(resourcePath, outputFile)
        }
        outputFile.toFile().setExecutable(true)
        return ResolvedBundledTool(path = outputFile)
    }

    private fun loadManifest(platform: BundledToolPlatform): BundledToolsManifest? {
        val resourcePath = "${platform.resourceDirectory}/manifest.properties"
        val resourceStream = classLoader.getResourceAsStream(resourcePath) ?: return null
        val properties = Properties()
        resourceStream.use(properties::load)
        val ffmpegVersion = properties.getProperty("ffmpegVersion") ?: return null
        val ytDlpVersion = properties.getProperty("ytDlpVersion") ?: return null
        return BundledToolsManifest(ffmpegVersion = ffmpegVersion, ytDlpVersion = ytDlpVersion)
    }

    private fun extractResource(resourcePath: String, outputFile: Path) {
        val resourceStream = classLoader.getResourceAsStream(resourcePath)
            ?: error("Bundled tool resource not found: $resourcePath")

        outputFile.parent.createDirectories()
        val tempFile = Files.createTempFile(outputFile.parent, outputFile.fileName.toString(), ".tmp")
        try {
            resourceStream.use { input ->
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING)
            }
            tempFile.toFile().setExecutable(true)
            Files.move(tempFile, outputFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }
}

internal data class ResolvedBundledTool(
    val path: Path
)

internal data class BundledToolsManifest(
    val ffmpegVersion: String,
    val ytDlpVersion: String
)

internal data class BundledToolPlatform(
    val directoryName: String
) {
    val resourceDirectory: String = "bundled-tools/$directoryName"

    companion object {
        fun currentOrNull(): BundledToolPlatform? {
            val osName = System.getProperty("os.name").orEmpty().lowercase()
            val arch = System.getProperty("os.arch").orEmpty().lowercase()
            if (!osName.contains("mac")) return null

            val normalizedArch = when (arch) {
                "aarch64", "arm64" -> "arm64"
                "x86_64", "amd64", "x64" -> "x64"
                else -> return null
            }
            return BundledToolPlatform(directoryName = "macos-$normalizedArch")
        }
    }
}

private fun String.toBundledToolName(): String? = when (this) {
    "ffmpeg", "yt-dlp" -> this
    else -> null
}

private fun defaultBundledToolsCacheRoot(): Path {
    val userHome = System.getProperty("user.home").orEmpty()
    return if (userHome.isNotBlank()) {
        Path.of(userHome, "Library", "Application Support", "iPodFeeder", "bundled-tools")
    } else {
        Path.of(System.getProperty("java.io.tmpdir"), "iPodFeeder", "bundled-tools")
    }
}
