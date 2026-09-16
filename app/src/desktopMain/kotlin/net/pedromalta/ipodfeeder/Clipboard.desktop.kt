package net.pedromalta.ipodfeeder

import java.awt.HeadlessException
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun copyTextToClipboard(text: String): ClipboardCopyResult = try {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    ClipboardCopyResult()
} catch (exception: IllegalStateException) {
    ClipboardCopyResult(exception.message ?: "Clipboard is temporarily unavailable.")
} catch (exception: HeadlessException) {
    ClipboardCopyResult(exception.message ?: "System clipboard is unavailable.")
}
