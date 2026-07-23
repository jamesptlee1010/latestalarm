package com.james.mathwakealarm

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TazNavy = Color(0xFF071A38)
val TazBlue = Color(0xFF1769E0)
val TazBlueLight = Color(0xFFEAF2FF)
val TazGreen = Color(0xFF159455)
val TazAmber = Color(0xFFF59E0B)
val TazRed = Color(0xFFDC2626)

private val LightColors = lightColorScheme(
    primary = TazBlue,
    onPrimary = Color.White,
    primaryContainer = TazBlueLight,
    onPrimaryContainer = TazNavy,
    secondary = Color(0xFF50647F),
    background = Color(0xFFF8FAFD),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F4F9),
    onBackground = TazNavy,
    onSurface = TazNavy,
    outline = Color(0xFFD3DCE8),
    error = TazRed
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5A9BFF),
    onPrimary = Color(0xFF001A41),
    primaryContainer = Color(0xFF10396F),
    onPrimaryContainer = Color(0xFFD7E6FF),
    secondary = Color(0xFFB6C8E4),
    background = Color(0xFF041225),
    surface = Color(0xFF0A1D35),
    surfaceVariant = Color(0xFF102944),
    onBackground = Color(0xFFF4F7FC),
    onSurface = Color(0xFFF4F7FC),
    outline = Color(0xFF3B526E),
    error = Color(0xFFFFB4AB)
)

@Composable
fun TazAlarmTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
