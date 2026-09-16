package net.pedromalta.ipodfeeder

import java.awt.HeadlessException
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

actual fun copyTextToClipboard(text: String): ClipboardCopyResult = try {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    ClipboardCopyResult()
} catch (exception: IllegalStateException) {
    ClipboardCopyResult(exception.message ?: "Clipboard is temporarily unavailable.")
} catch (exception: HeadlessException) {
    ClipboardCopyResult(exception.message ?: "System clipboard is unavailable.")
}

actual fun pasteTextFromClipboard(): ClipboardPasteResult = try {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        ClipboardPasteResult(errorMessage = "Clipboard does not contain text.")
    } else {
        ClipboardPasteResult(text = clipboard.getData(DataFlavor.stringFlavor) as? String)
    }
} catch (exception: IllegalStateException) {
    ClipboardPasteResult(errorMessage = exception.message ?: "Clipboard is temporarily unavailable.")
} catch (exception: HeadlessException) {
    ClipboardPasteResult(errorMessage = exception.message ?: "System clipboard is unavailable.")
} catch (exception: UnsupportedFlavorException) {
    ClipboardPasteResult(errorMessage = exception.message ?: "Clipboard text format is unsupported.")
} catch (exception: IOException) {
    ClipboardPasteResult(errorMessage = exception.message ?: "Could not read clipboard text.")
}
