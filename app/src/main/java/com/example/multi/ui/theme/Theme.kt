package com.example.multi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Plum80,
    secondary = LavenderGlow,
    tertiary = AzureAccent,
    background = MidnightSurface,
    surface = MidnightSurface,
    surfaceVariant = MidnightOverlay,
    onPrimary = Color(0xFF140824),
    onSecondary = Color(0xFF0E0818),
    onTertiary = Color(0xFF0E0818),
    onBackground = Color(0xFFEDE7F6),
    onSurface = Color(0xFFEDE7F6),
    outline = Color(0xFF7C6AA6)
)

private val LightColorScheme = lightColorScheme(
    primary = Plum40,
    secondary = LavenderGlow,
    tertiary = AzureAccent,
    background = MidnightAlt,
    surface = MidnightAlt,
    surfaceVariant = MidnightOverlay,
    onPrimary = Color(0xFF0F071C),
    onSecondary = Color(0xFF0E0818),
    onTertiary = Color(0xFF0E0818),
    onBackground = Color(0xFFF5EEFF),
    onSurface = Color(0xFFF5EEFF),
    outline = Color(0xFF9E8BD2)
)

/**
 * Applies the application's color scheme and typography to [content].
 *
 * When available, dynamic colors from the system are used.
 */
@Composable
fun MultiTheme(
    darkTheme: Boolean = true,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
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
        content = content
    )
}
