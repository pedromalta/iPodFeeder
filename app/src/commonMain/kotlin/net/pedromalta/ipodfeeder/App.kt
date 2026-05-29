package net.pedromalta.ipodfeeder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.pedromalta.ipodfeeder.audio.AudioProcessingRequest
import net.pedromalta.ipodfeeder.audio.createAudioProcessingEngine
import net.pedromalta.ipodfeeder.audio.defaultOutputDirectory
import net.pedromalta.ipodfeeder.ui.theme.IPodFeederTheme

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val engine = remember { createAudioProcessingEngine() }

    var url by remember { mutableStateOf("") }
    var outputDirectory by remember { mutableStateOf(defaultOutputDirectory()) }
    var addToMusicLibrary by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val logLines = remember { mutableStateListOf<String>() }

    IPodFeederTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("iPod Feeder", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Paste a YouTube URL, convert to MP3, and keep files ready for Music import.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("YouTube URL") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing,
                singleLine = true
            )

            OutlinedTextField(
                value = outputDirectory,
                onValueChange = { outputDirectory = it },
                label = { Text("Output directory") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing,
                singleLine = true
            )

            Row {
                Checkbox(
                    checked = addToMusicLibrary,
                    onCheckedChange = { addToMusicLibrary = it },
                    enabled = !isProcessing
                )
                Text("Automatically add converted track to Music app")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !isProcessing && url.isNotBlank() && outputDirectory.isNotBlank(),
                    onClick = {
                        isProcessing = true
                        resultMessage = null
                        logLines.clear()
                        scope.launch {
                            runCatching {
                                engine.process(
                                    request = AudioProcessingRequest(
                                        youtubeUrl = url.trim(),
                                        outputDirectory = outputDirectory.trim(),
                                        addToMusicLibrary = addToMusicLibrary
                                    ),
                                    onProgress = { message -> logLines.add(message) }
                                )
                            }.onSuccess { result ->
                                val musicImportStatus = if (result.addedToMusicLibrary) {
                                    " and imported it into Music"
                                } else {
                                    ""
                                }
                                resultMessage = "Saved ${result.title} by ${result.artist} to ${result.outputFilePath}$musicImportStatus"
                            }.onFailure { error ->
                                resultMessage = "Failed: ${error.message ?: "Unknown error"}"
                            }
                            isProcessing = false
                        }
                    }
                ) {
                    Text("Convert to MP3")
                }

                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 10.dp))
                }
            }

            resultMessage?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            if (logLines.isNotEmpty()) {
                Text("Progress", style = MaterialTheme.typography.titleMedium)
                Text(logLines.joinToString(separator = "\n"), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
