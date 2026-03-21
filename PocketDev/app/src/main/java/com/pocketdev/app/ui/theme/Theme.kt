package com.pocketdev.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark theme palette - VSCode inspired
private val DarkColorScheme = darkColorScheme(
    primary = DarkThemeColors.primary,
    onPrimary = DarkThemeColors.onPrimary,
    primaryContainer = DarkThemeColors.primaryContainer,
    onPrimaryContainer = DarkThemeColors.onPrimaryContainer,
    secondary = DarkThemeColors.secondary,
    onSecondary = DarkThemeColors.onSecondary,
    secondaryContainer = DarkThemeColors.secondaryContainer,
    onSecondaryContainer = DarkThemeColors.onSecondaryContainer,
    tertiary = DarkThemeColors.tertiary,
    onTertiary = DarkThemeColors.onTertiary,
    tertiaryContainer = DarkThemeColors.tertiaryContainer,
    onTertiaryContainer = DarkThemeColors.onTertiaryContainer,
    error = DarkThemeColors.error,
    onError = DarkThemeColors.onError,
    errorContainer = DarkThemeColors.errorContainer,
    onErrorContainer = DarkThemeColors.onErrorContainer,
    background = DarkThemeColors.background,
    onBackground = DarkThemeColors.onBackground,
    surface = DarkThemeColors.surface,
    onSurface = DarkThemeColors.onSurface,
    surfaceVariant = DarkThemeColors.surfaceVariant,
    onSurfaceVariant = DarkThemeColors.onSurfaceVariant,
    outline = DarkThemeColors.outline,
    outlineVariant = DarkThemeColors.outlineVariant,
    inverseSurface = DarkThemeColors.inverseSurface,
    inverseOnSurface = DarkThemeColors.inverseOnSurface,
    inversePrimary = DarkThemeColors.inversePrimary
)

// Light theme palette - Professional
private val LightColorScheme = lightColorScheme(
    primary = LightThemeColors.primary,
    onPrimary = LightThemeColors.onPrimary,
    primaryContainer = LightThemeColors.primaryContainer,
    onPrimaryContainer = LightThemeColors.onPrimaryContainer,
    secondary = LightThemeColors.secondary,
    onSecondary = LightThemeColors.onSecondary,
    secondaryContainer = LightThemeColors.secondaryContainer,
    onSecondaryContainer = LightThemeColors.onSecondaryContainer,
    tertiary = LightThemeColors.tertiary,
    onTertiary = LightThemeColors.onTertiary,
    tertiaryContainer = LightThemeColors.tertiaryContainer,
    onTertiaryContainer = LightThemeColors.onTertiaryContainer,
    error = LightThemeColors.error,
    onError = LightThemeColors.onError,
    errorContainer = LightThemeColors.errorContainer,
    onErrorContainer = LightThemeColors.onErrorContainer,
    background = LightThemeColors.background,
    onBackground = LightThemeColors.onBackground,
    surface = LightThemeColors.surface,
    onSurface = LightThemeColors.onSurface,
    surfaceVariant = LightThemeColors.surfaceVariant,
    onSurfaceVariant = LightThemeColors.onSurfaceVariant,
    outline = LightThemeColors.outline,
    outlineVariant = LightThemeColors.outlineVariant,
    inverseSurface = LightThemeColors.inverseSurface,
    inverseOnSurface = LightThemeColors.inverseOnSurface,
    inversePrimary = LightThemeColors.inversePrimary
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
