package com.example.multi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OrchidPrimary,
    onPrimary = Color.White,
    secondary = LavenderSecondary,
    onSecondary = Color.White,
    tertiary = AmethystTertiary,
    background = NightPurple,
    onBackground = LilacHighlight,
    surface = GrapeSurface,
    onSurface = LilacHighlight,
    surfaceVariant = VelvetPurple,
    onSurfaceVariant = LilacHighlight,
    secondaryContainer = VelvetPurple,
    onSecondaryContainer = LilacHighlight,
    outline = PlumOutline,
    inversePrimary = LilacHighlight
)

private val LightColorScheme = lightColorScheme(
    primary = OrchidPrimary,
    onPrimary = Color.White,
    secondary = LavenderSecondary,
    onSecondary = Color.White,
    tertiary = AmethystTertiary,
    background = NightPurple,
    onBackground = LilacHighlight,
    surface = GrapeSurface,
    onSurface = LilacHighlight,
    surfaceVariant = VelvetPurple,
    onSurfaceVariant = LilacHighlight,
    secondaryContainer = VelvetPurple,
    onSecondaryContainer = LilacHighlight,
    outline = PlumOutline,
    inversePrimary = LilacHighlight
)

/**
 * Applies the application's color scheme and typography to [content].
 *
 * When available, dynamic colors from the system are used.
 */
@Composable
fun MultiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
