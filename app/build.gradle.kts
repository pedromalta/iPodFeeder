import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

data class BundledBinary(
    val downloadUrl: String,
    val sha256: String
)

fun normalizeMacArch(arch: String): String = when (arch.lowercase()) {
    "aarch64", "arm64" -> "arm64"
    "x86_64", "amd64", "x64" -> "x64"
    else -> error("Unsupported macOS architecture for bundled tools: $arch")
}

fun sha256Of(file: java.io.File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().buffered().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

fun downloadBinary(target: java.io.File, binary: BundledBinary) {
    target.parentFile.mkdirs()
    val tempFile = Files.createTempFile(target.parentFile.toPath(), target.name, ".download").toFile()
    try {
        URI(binary.downloadUrl).toURL().openStream().use { input ->
            Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        val actualSha = sha256Of(tempFile)
        check(actualSha == binary.sha256) {
            "Checksum mismatch for ${target.name}. Expected ${binary.sha256}, got $actualSha."
        }
        Files.move(
            tempFile.toPath(),
            target.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE
        )
        target.setExecutable(true)
    } finally {
        tempFile.delete()
    }
}

val bundledYtDlpVersion = "2026.07.04"
val bundledYtDlp = BundledBinary(
    downloadUrl = "https://github.com/yt-dlp/yt-dlp/releases/download/$bundledYtDlpVersion/yt-dlp",
    sha256 = "495be29ff4d9d4e9be7eabdfef225221e5d5282e77f2f505abc6dca80349f3fd"
)

val bundledFfmpegVersion = "b6.1.1"
val bundledMacArch = normalizeMacArch(System.getProperty("os.arch"))
val bundledFfmpeg = when (bundledMacArch) {
    "arm64" -> BundledBinary(
        downloadUrl = "https://github.com/eugeneware/ffmpeg-static/releases/download/$bundledFfmpegVersion/ffmpeg-darwin-arm64",
        sha256 = "a90e3db6a3fd35f6074b013f948b1aa45b31c6375489d39e572bea3f18336584"
    )
    "x64" -> BundledBinary(
        downloadUrl = "https://github.com/eugeneware/ffmpeg-static/releases/download/$bundledFfmpegVersion/ffmpeg-darwin-x64",
        sha256 = "ebdddc936f61e14049a2d4b549a412b8a40deeff6540e58a9f2a2da9e6b18894"
    )
    else -> error("Unsupported macOS architecture for bundled tools: $bundledMacArch")
}

val bundledDesktopToolsDir = layout.buildDirectory.dir("generated/bundledDesktopTools")

val prepareBundledDesktopTools = tasks.register("prepareBundledDesktopTools") {
    val outputDir = bundledDesktopToolsDir
    outputs.dir(outputDir)
    inputs.property("bundledMacArch", bundledMacArch)
    inputs.property("bundledFfmpegVersion", bundledFfmpegVersion)
    inputs.property("bundledYtDlpVersion", bundledYtDlpVersion)

    doLast {
        val toolsDir = outputDir.get().dir("bundled-tools/macos-$bundledMacArch").asFile
        delete(outputDir)
        toolsDir.mkdirs()

        downloadBinary(toolsDir.resolve("ffmpeg"), bundledFfmpeg)
        downloadBinary(toolsDir.resolve("yt-dlp"), bundledYtDlp)

        val manifest = Properties().apply {
            setProperty("ffmpegVersion", bundledFfmpegVersion)
            setProperty("ytDlpVersion", bundledYtDlpVersion)
            setProperty("platform", "macos")
            setProperty("arch", bundledMacArch)
        }
        toolsDir.resolve("manifest.properties").outputStream().use { output ->
            manifest.store(output, null)
        }
    }
}

kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.core)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
            }
        }
        val desktopMain by getting {
            resources.srcDir(bundledDesktopToolsDir)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.okhttp)
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

tasks.matching { it.name in setOf("desktopProcessResources", "desktopTestProcessResources") }
    .configureEach {
        dependsOn(prepareBundledDesktopTools)
    }

compose.desktop {
    application {
        mainClass = "net.pedromalta.ipodfeeder.MainKt"
        javaHome = "/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home"

        nativeDistributions {
            packageName = "iPodFeeder"
            targetFormats(TargetFormat.Dmg)
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/desktopMain/resources"))
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
        }
    }
}