package net.pedromalta.ipodfeeder

data class ClipboardCopyResult(
    val errorMessage: String? = null
)

expect fun copyTextToClipboard(text: String): ClipboardCopyResult

data class ClipboardPasteResult(
    val text: String? = null,
    val errorMessage: String? = null
)

expect fun pasteTextFromClipboard(): ClipboardPasteResult
