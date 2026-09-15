package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.pref.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = GeminiDarkPrimary,
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468B),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = GeminiDarkSecondary,
    onSecondary = Color(0xFF003353),
    secondaryContainer = Color(0xFF004A75),
    onSecondaryContainer = Color(0xFFCCE5FF),
    tertiary = GeminiDarkTertiary,
    onTertiary = Color(0xFF381E72),
    background = GeminiDarkBackground,
    onBackground = GeminiDarkTextPrimary,
    surface = GeminiDarkBackground,
    onSurface = GeminiDarkTextPrimary,
    surfaceVariant = GeminiDarkSurface,
    onSurfaceVariant = GeminiDarkTextSecondary,
    outline = GeminiDarkBorder,
    outlineVariant = Color(0xFF444746)
)

private val LightColorScheme = lightColorScheme(
    primary = GeminiLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = GeminiLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE5FF),
    onSecondaryContainer = Color(0xFF001E31),
    tertiary = GeminiLightTertiary,
    onTertiary = Color.White,
    background = GeminiLightBackground,
    onBackground = GeminiLightTextPrimary,
    surface = GeminiLightBackground,
    onSurface = GeminiLightTextPrimary,
    surfaceVariant = GeminiLightSurface,
    onSurfaceVariant = GeminiLightTextSecondary,
    outline = GeminiLightBorder,
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun GeminiChatTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backward compatibility for tests
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
