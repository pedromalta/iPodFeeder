package net.pedromalta.ipodfeeder

data class ClipboardCopyResult(
    val errorMessage: String? = null
)

expect fun copyTextToClipboard(text: String): ClipboardCopyResult
