package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MetronomiconColorScheme = darkColorScheme(
    primary = WarmOrangePrimary,
    onPrimary = Color(0xFF1E0E00),
    primaryContainer = Color(0xFF3E1A00),
    onPrimaryContainer = WarmOrangeLight,
    secondary = CyanAccent,
    onSecondary = Color(0xFF002026),
    secondaryContainer = Color(0xFF003640),
    onSecondaryContainer = CyanGlow,
    tertiary = CrimsonAccent,
    onTertiary = Color(0xFF3B0008),
    tertiaryContainer = Color(0xFF5C0012),
    onTertiaryContainer = CrimsonGlow,
    background = MetronomeBg,
    onBackground = TextPrimary,
    surface = MetronomeSurface,
    onSurface = TextPrimary,
    surfaceVariant = MetronomeSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = MetronomeBorder,
    outlineVariant = MetronomeSurfaceHigh
)

@Composable
fun MetronomiconTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MetronomiconColorScheme,
        typography = Typography,
        content = content
    )
}
