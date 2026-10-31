package com.arekb.aqua.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // TODO: Add a toggle off in settings
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}
