package com.pocketdev.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark theme palette
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    onPrimary = Color(0xFF003E5C),
    primaryContainer = Color(0xFF004D6E),
    onPrimaryContainer = Color(0xFFB3E5FC),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF003A14),
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF442B00),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF690005),
    background = Color(0xFF0D1117),
    onBackground = Color(0xFFE6EDF3),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFFCDD9E5),
    outline = Color(0xFF30363D),
    outlineVariant = Color(0xFF21262D),
    inverseSurface = Color(0xFFE6EDF3),
    inverseOnSurface = Color(0xFF161B22)
)

// Light theme palette
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0277BD),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB3E5FC),
    onPrimaryContainer = Color(0xFF003E5C),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF003A14),
    tertiary = Color(0xFFE65100),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFB71C1C),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1A2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0)
)

@Composable
fun PocketDevTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
