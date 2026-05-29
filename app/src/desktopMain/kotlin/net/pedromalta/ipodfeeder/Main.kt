package net.pedromalta.ipodfeeder

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import `ipod feeder`.app.generated.resources.Res
import `ipod feeder`.app.generated.resources.icon
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    val icon = painterResource(Res.drawable.icon)
    Window(
        onCloseRequest = ::exitApplication,
        title = "iPod Feeder",
        icon = icon,
    ) {
        // Apply macOS specific window hints
        window.rootPane.apply {
            putClientProperty("apple.awt.fullWindowContent", true)
            putClientProperty("apple.awt.transparentTitleBar", true)
            putClientProperty("apple.awt.windowTitleVisible", false)
        }
        App()
    }
}
