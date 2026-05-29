package net.pedromalta.ipodfeeder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentLight,
    onPrimary = Color.White,
    secondary = SecondaryAccent,
    onSecondary = DarkBackground,
    tertiary = SecondaryAccent,
    onTertiary = DarkBackground,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSecondaryBg,
    onBackground = OnDark,
    onSurface = OnDark,
    outline = DarkMutedText
)

private val LightColorScheme = lightColorScheme(
    primary = AccentDark,
    onPrimary = Color.White,
    secondary = SecondaryAccentLight,
    onSecondary = Color.White,
    tertiary = SecondaryAccentLight,
    onTertiary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightBackground,
    onBackground = OnLight,
    onSurface = OnLight,
    outline = LightMutedText
)

@Composable
fun IPodFeederTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

