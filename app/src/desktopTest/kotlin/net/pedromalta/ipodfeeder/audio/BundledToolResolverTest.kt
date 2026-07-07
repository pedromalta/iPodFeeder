package net.pedromalta.ipodfeeder.audio

import java.net.URLClassLoader
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BundledToolResolverTest {
    @Test
    fun extractsBundledToolIntoVersionedCacheDirectory() {
        val resourcesRoot = createTempDirectory("bundled-tool-resources")
        val resourceDirectory = resourcesRoot.resolve("bundled-tools/macos-arm64")
        resourceDirectory.createDirectories()

        resourceDirectory.resolve("ffmpeg").outputStream().use { it.write("#!/bin/sh\necho ffmpeg\n".toByteArray()) }
        resourceDirectory.resolve("manifest.properties").outputStream().use {
            it.write(
                """
                ffmpegVersion=b6.1.1
                ytDlpVersion=2026.07.04
                """.trimIndent().toByteArray()
            )
        }

        val cacheRoot = createTempDirectory("bundled-tool-cache")
        val classLoader = URLClassLoader(arrayOf(resourcesRoot.toUri().toURL()), null)
        val resolver = BundledToolResolver(
            classLoader = classLoader,
            cacheRoot = cacheRoot,
            platform = BundledToolPlatform("macos-arm64")
        )

        val bundledTool = resolver.resolve("ffmpeg")

        assertEquals(
            cacheRoot.resolve("macos-arm64-b6.1.1-2026.07.04/ffmpeg").toString(),
            bundledTool?.path.toString()
        )
        assertTrue(bundledTool != null)
        assertTrue(bundledTool.path.toFile().canExecute())
        assertEquals("#!/bin/sh\necho ffmpeg\n", bundledTool.path.readText())
    }
}
