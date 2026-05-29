package net.pedromalta.ipodfeeder.audio

import java.nio.file.Paths

actual fun createAudioProcessingEngine(): AudioProcessingEngine = DesktopAudioProcessingEngine()

actual fun defaultOutputDirectory(): String {
	return Paths.get(System.getProperty("user.home"), "Music", "iPod Feeder", "Imports").toString()
}

