package com.example.multi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/* ----------------------------------------------------------------------------
 * Material 3 schemes built from the brand seed
 * ------------------------------------------------------------------------- */

private val LightColors = lightColorScheme(
    primary = md_light_primary,
    onPrimary = md_light_onPrimary,
    primaryContainer = md_light_primaryContainer,
    onPrimaryContainer = md_light_onPrimaryContainer,
    secondary = md_light_secondary,
    onSecondary = md_light_onSecondary,
    secondaryContainer = md_light_secondaryContainer,
    onSecondaryContainer = md_light_onSecondaryContainer,
    tertiary = md_light_tertiary,
    onTertiary = md_light_onTertiary,
    tertiaryContainer = md_light_tertiaryContainer,
    onTertiaryContainer = md_light_onTertiaryContainer,
    error = md_light_error,
    onError = md_light_onError,
    errorContainer = md_light_errorContainer,
    onErrorContainer = md_light_onErrorContainer,
    background = md_light_background,
    onBackground = md_light_onBackground,
    surface = md_light_surface,
    onSurface = md_light_onSurface,
    surfaceVariant = md_light_surfaceVariant,
    onSurfaceVariant = md_light_onSurfaceVariant,
    outline = md_light_outline,
    outlineVariant = md_light_outlineVariant,
    scrim = md_light_scrim,
    inverseSurface = md_light_inverseSurface,
    inverseOnSurface = md_light_inverseOnSurface,
    inversePrimary = md_light_inversePrimary,
    surfaceDim = md_light_surfaceDim,
    surfaceBright = md_light_surfaceBright,
    surfaceContainerLowest = md_light_surfaceContainerLowest,
    surfaceContainerLow = md_light_surfaceContainerLow,
    surfaceContainer = md_light_surfaceContainer,
    surfaceContainerHigh = md_light_surfaceContainerHigh,
    surfaceContainerHighest = md_light_surfaceContainerHighest,
)

private val DarkColors = darkColorScheme(
    primary = md_dark_primary,
    onPrimary = md_dark_onPrimary,
    primaryContainer = md_dark_primaryContainer,
    onPrimaryContainer = md_dark_onPrimaryContainer,
    secondary = md_dark_secondary,
    onSecondary = md_dark_onSecondary,
    secondaryContainer = md_dark_secondaryContainer,
    onSecondaryContainer = md_dark_onSecondaryContainer,
    tertiary = md_dark_tertiary,
    onTertiary = md_dark_onTertiary,
    tertiaryContainer = md_dark_tertiaryContainer,
    onTertiaryContainer = md_dark_onTertiaryContainer,
    error = md_dark_error,
    onError = md_dark_onError,
    errorContainer = md_dark_errorContainer,
    onErrorContainer = md_dark_onErrorContainer,
    background = md_dark_background,
    onBackground = md_dark_onBackground,
    surface = md_dark_surface,
    onSurface = md_dark_onSurface,
    surfaceVariant = md_dark_surfaceVariant,
    onSurfaceVariant = md_dark_onSurfaceVariant,
    outline = md_dark_outline,
    outlineVariant = md_dark_outlineVariant,
    scrim = md_dark_scrim,
    inverseSurface = md_dark_inverseSurface,
    inverseOnSurface = md_dark_inverseOnSurface,
    inversePrimary = md_dark_inversePrimary,
    surfaceDim = md_dark_surfaceDim,
    surfaceBright = md_dark_surfaceBright,
    surfaceContainerLowest = md_dark_surfaceContainerLowest,
    surfaceContainerLow = md_dark_surfaceContainerLow,
    surfaceContainer = md_dark_surfaceContainer,
    surfaceContainerHigh = md_dark_surfaceContainerHigh,
    surfaceContainerHighest = md_dark_surfaceContainerHighest,
)

/* ----------------------------------------------------------------------------
 * Extended colors — everything the M3 spec doesn't give us a slot for
 * ------------------------------------------------------------------------- */

/** One recognisable accent per feature area. */
data class SegmentColors(
    val color: Color,
    val container: Color,
)

@Suppress("unused")
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val notes: SegmentColors,
    val events: SegmentColors,
    val calendar: SegmentColors,
    val goals: SegmentColors,
)

private val LightExtended = ExtendedColors(
    success = ext_light_success,
    onSuccess = ext_light_onSuccess,
    successContainer = ext_light_successContainer,
    onSuccessContainer = ext_light_onSuccessContainer,
    warning = ext_light_warning,
    onWarning = ext_light_onWarning,
    warningContainer = ext_light_warningContainer,
    onWarningContainer = ext_light_onWarningContainer,
    notes = SegmentColors(seg_light_notes, seg_light_notesContainer),
    events = SegmentColors(seg_light_events, seg_light_eventsContainer),
    calendar = SegmentColors(seg_light_calendar, seg_light_calendarContainer),
    goals = SegmentColors(seg_light_goals, seg_light_goalsContainer),
)

private val DarkExtended = ExtendedColors(
    success = ext_dark_success,
    onSuccess = ext_dark_onSuccess,
    successContainer = ext_dark_successContainer,
    onSuccessContainer = ext_dark_onSuccessContainer,
    warning = ext_dark_warning,
    onWarning = ext_dark_onWarning,
    warningContainer = ext_dark_warningContainer,
    onWarningContainer = ext_dark_onWarningContainer,
    notes = SegmentColors(seg_dark_notes, seg_dark_notesContainer),
    events = SegmentColors(seg_dark_events, seg_dark_eventsContainer),
    calendar = SegmentColors(seg_dark_calendar, seg_dark_calendarContainer),
    goals = SegmentColors(seg_dark_goals, seg_dark_goalsContainer),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

/* ----------------------------------------------------------------------------
 * Theme entry point
 * ------------------------------------------------------------------------- */

/**
 * Applies Multi's color scheme, typography, shapes and spacing to [content].
 *
 * @param dynamicColor when true (Android 12+) the OS wallpaper palette is used
 *   instead of the brand palette. Default **false** — Multi has a deliberate
 *   brand identity and we want it to look the same on every device — but the
 *   hook is left in place so a "Use device colors" setting can flip it on.
 */
@Composable
fun MultiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    val extendedColors = if (darkTheme) DarkExtended else LightExtended

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = colorScheme.background.luminance() > 0.5f
                isAppearanceLightNavigationBars = colorScheme.background.luminance() > 0.5f
            }
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.toArgb()
        }
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

/** Convenience accessors: `MultiTheme.extended.success`, `MultiTheme.spacing.lg`. */
object MultiTheme {
    val extended: ExtendedColors
        @Composable get() = LocalExtendedColors.current

    val spacing: Spacing
        @Composable get() = LocalSpacing.current
}
