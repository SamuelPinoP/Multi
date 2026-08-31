package com.example.multi.ui.theme

/**
 * Multi color system.
 *
 * The palette is derived from a single brand seed — an expressive indigo
 * (`#4C3BCF`) — expanded into a full Material 3 tonal scheme for light and
 * dark. On top of the standard roles we layer a small set of *extended*
 * semantic colors (success / warning) and four *segment* accents that give
 * Notes, Events, Calendar and Weekly Goals their own recognisable identity
 * across the app.
 *
 * Everything is referenced through [MaterialTheme.colorScheme] or
 * [com.example.multi.ui.theme.MultiTheme.extended] — no screen should hard-code
 * a hex value.
 */

import androidx.compose.ui.graphics.Color

/* ----------------------------------------------------------------------------
 * Brand seed
 * ------------------------------------------------------------------------- */

/** The single hue everything else is generated from. */
val BrandIndigo = Color(0xFF4C3BCF)

/* ----------------------------------------------------------------------------
 * Light scheme roles
 * ------------------------------------------------------------------------- */

internal val md_light_primary = Color(0xFF4A3BC4)
internal val md_light_onPrimary = Color(0xFFFFFFFF)
internal val md_light_primaryContainer = Color(0xFFE4DFFF)
internal val md_light_onPrimaryContainer = Color(0xFF160277)
internal val md_light_secondary = Color(0xFF5C5B72)
internal val md_light_onSecondary = Color(0xFFFFFFFF)
internal val md_light_secondaryContainer = Color(0xFFE2DFF9)
internal val md_light_onSecondaryContainer = Color(0xFF191A2C)
internal val md_light_tertiary = Color(0xFF984061)
internal val md_light_onTertiary = Color(0xFFFFFFFF)
internal val md_light_tertiaryContainer = Color(0xFFFFD9E2)
internal val md_light_onTertiaryContainer = Color(0xFF3E001D)
internal val md_light_error = Color(0xFFBA1A1A)
internal val md_light_onError = Color(0xFFFFFFFF)
internal val md_light_errorContainer = Color(0xFFFFDAD6)
internal val md_light_onErrorContainer = Color(0xFF410002)
internal val md_light_background = Color(0xFFFCF8FF)
internal val md_light_onBackground = Color(0xFF1B1B21)
internal val md_light_surface = Color(0xFFFCF8FF)
internal val md_light_onSurface = Color(0xFF1B1B21)
internal val md_light_surfaceVariant = Color(0xFFE4E1EC)
internal val md_light_onSurfaceVariant = Color(0xFF47464F)
internal val md_light_outline = Color(0xFF78767F)
internal val md_light_outlineVariant = Color(0xFFC8C5D0)
internal val md_light_scrim = Color(0xFF000000)
internal val md_light_inverseSurface = Color(0xFF303036)
internal val md_light_inverseOnSurface = Color(0xFFF3EFF7)
internal val md_light_inversePrimary = Color(0xFFC7BFFF)
internal val md_light_surfaceDim = Color(0xFFDCD9E0)
internal val md_light_surfaceBright = Color(0xFFFCF8FF)
internal val md_light_surfaceContainerLowest = Color(0xFFFFFFFF)
internal val md_light_surfaceContainerLow = Color(0xFFF6F2FA)
internal val md_light_surfaceContainer = Color(0xFFF0ECF4)
internal val md_light_surfaceContainerHigh = Color(0xFFEAE7EF)
internal val md_light_surfaceContainerHighest = Color(0xFFE5E1E9)

/* ----------------------------------------------------------------------------
 * Dark scheme roles
 * ------------------------------------------------------------------------- */

internal val md_dark_primary = Color(0xFFC7BFFF)
internal val md_dark_onPrimary = Color(0xFF2C179C)
internal val md_dark_primaryContainer = Color(0xFF3A29B4)
internal val md_dark_onPrimaryContainer = Color(0xFFE4DFFF)
internal val md_dark_secondary = Color(0xFFC6C3DD)
internal val md_dark_onSecondary = Color(0xFF2E2E42)
internal val md_dark_secondaryContainer = Color(0xFF444459)
internal val md_dark_onSecondaryContainer = Color(0xFFE2DFF9)
internal val md_dark_tertiary = Color(0xFFFFB1C8)
internal val md_dark_onTertiary = Color(0xFF5E1133)
internal val md_dark_tertiaryContainer = Color(0xFF7B2949)
internal val md_dark_onTertiaryContainer = Color(0xFFFFD9E2)
internal val md_dark_error = Color(0xFFFFB4AB)
internal val md_dark_onError = Color(0xFF690005)
internal val md_dark_errorContainer = Color(0xFF93000A)
internal val md_dark_onErrorContainer = Color(0xFFFFDAD6)
internal val md_dark_background = Color(0xFF131318)
internal val md_dark_onBackground = Color(0xFFE5E1E9)
internal val md_dark_surface = Color(0xFF131318)
internal val md_dark_onSurface = Color(0xFFE5E1E9)
internal val md_dark_surfaceVariant = Color(0xFF47464F)
internal val md_dark_onSurfaceVariant = Color(0xFFC8C5D0)
internal val md_dark_outline = Color(0xFF928F9A)
internal val md_dark_outlineVariant = Color(0xFF47464F)
internal val md_dark_scrim = Color(0xFF000000)
internal val md_dark_inverseSurface = Color(0xFFE5E1E9)
internal val md_dark_inverseOnSurface = Color(0xFF303036)
internal val md_dark_inversePrimary = Color(0xFF4A3BC4)
internal val md_dark_surfaceDim = Color(0xFF131318)
internal val md_dark_surfaceBright = Color(0xFF39383F)
internal val md_dark_surfaceContainerLowest = Color(0xFF0E0E13)
internal val md_dark_surfaceContainerLow = Color(0xFF1B1B21)
internal val md_dark_surfaceContainer = Color(0xFF1F1F25)
internal val md_dark_surfaceContainerHigh = Color(0xFF2A292F)
internal val md_dark_surfaceContainerHighest = Color(0xFF35343A)

/* ----------------------------------------------------------------------------
 * Extended semantic colors (not part of the M3 spec)
 * ------------------------------------------------------------------------- */

/** "Positive / done" signal — greener than tertiary, used for completion. */
internal val ext_light_success = Color(0xFF326B3E)
internal val ext_light_onSuccess = Color(0xFFFFFFFF)
internal val ext_light_successContainer = Color(0xFFB4F1B9)
internal val ext_light_onSuccessContainer = Color(0xFF00210A)

internal val ext_light_warning = Color(0xFF8C5000)
internal val ext_light_onWarning = Color(0xFFFFFFFF)
internal val ext_light_warningContainer = Color(0xFFFFDCBE)
internal val ext_light_onWarningContainer = Color(0xFF2D1600)

internal val ext_dark_success = Color(0xFF99D49F)
internal val ext_dark_onSuccess = Color(0xFF00391B)
internal val ext_dark_successContainer = Color(0xFF175129)
internal val ext_dark_onSuccessContainer = Color(0xFFB4F1B9)

internal val ext_dark_warning = Color(0xFFFFB870)
internal val ext_dark_onWarning = Color(0xFF4A2800)
internal val ext_dark_warningContainer = Color(0xFF6A3C00)
internal val ext_dark_onWarningContainer = Color(0xFFFFDCBE)

/* ----------------------------------------------------------------------------
 * Segment accents — one identity per feature.
 * Used for list-item avatars, medallion glows, chips and empty-state art.
 * ------------------------------------------------------------------------- */

// Notes — "ice" blue
internal val seg_light_notes = Color(0xFF1B6EF3)
internal val seg_light_notesContainer = Color(0xFFD7E3FF)
internal val seg_dark_notes = Color(0xFFAAC7FF)
internal val seg_dark_notesContainer = Color(0xFF00458E)

// Events — "lava" warm red
internal val seg_light_events = Color(0xFFB3261E)
internal val seg_light_eventsContainer = Color(0xFFFFDAD5)
internal val seg_dark_events = Color(0xFFFFB4AB)
internal val seg_dark_eventsContainer = Color(0xFF8C0009)

// Calendar — "rock" slate
internal val seg_light_calendar = Color(0xFF4C5B72)
internal val seg_light_calendarContainer = Color(0xFFD9E3F8)
internal val seg_dark_calendar = Color(0xFFB4C6E7)
internal val seg_dark_calendarContainer = Color(0xFF344559)

// Weekly Goals — "moss" green
internal val seg_light_goals = Color(0xFF2E6B4F)
internal val seg_light_goalsContainer = Color(0xFFB4F1CF)
internal val seg_dark_goals = Color(0xFF99D5B3)
internal val seg_dark_goalsContainer = Color(0xFF14513A)

/* Legacy names kept so the calendar grid keeps compiling until it is migrated. */
val CalendarTodayBg = seg_light_goalsContainer
val CalendarTodayBorder = seg_light_goals
