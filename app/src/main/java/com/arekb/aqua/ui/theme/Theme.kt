package com.arekb.aqua.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8CD7F4),
    onPrimary = Color(0xFF003545),
    primaryContainer = Color(0xFF14566C),
    onPrimaryContainer = Color(0xFFC3EDFF),
    secondary = Color(0xFFB7D9E5),
    tertiary = Color(0xFFBCE9D4),
    background = Color(0xFF10191D),
    surface = Color(0xFF10191D),
    surfaceContainerLow = Color(0xFF19252A),
    surfaceContainerHighest = Color(0xFF314047)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006782),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB9EAFF),
    onPrimaryContainer = Color(0xFF001F29),
    secondary = Color(0xFF45616C),
    tertiary = Color(0xFF426458),
    background = Color(0xFFF4F8FA),
    surface = Color(0xFFF4F8FA),
    surfaceContainerLow = Color(0xFFEAF1F4),
    surfaceContainerHighest = Color(0xFFD7E2E7)
)

@Composable
fun AquaTheme(
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
