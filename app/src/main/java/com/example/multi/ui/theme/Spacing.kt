package com.example.multi.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * An 8-point spacing scale. Using named steps instead of raw `dp` literals
 * keeps rhythm consistent between screens and makes intent obvious at the call
 * site (`Spacer(Modifier.height(MultiTheme.spacing.lg))`).
 */
data class Spacing(
    /** 2 dp — hairline gaps, icon-to-text nudges. */
    val xxs: Dp = 2.dp,
    /** 4 dp — tight internal padding. */
    val xs: Dp = 4.dp,
    /** 8 dp — default gap between related elements. */
    val sm: Dp = 8.dp,
    /** 12 dp — gap between list items. */
    val md: Dp = 12.dp,
    /** 16 dp — screen edge gutter, card padding. */
    val lg: Dp = 16.dp,
    /** 24 dp — section separation. */
    val xl: Dp = 24.dp,
    /** 32 dp — major blocks, empty-state breathing room. */
    val xxl: Dp = 32.dp,
    /** 48 dp — hero spacing. */
    val xxxl: Dp = 48.dp,
    /** Standard screen gutter. */
    val gutter: Dp = 16.dp,
    /** Height reserved at the bottom of scroll content so a FAB never covers it. */
    val fabClearance: Dp = 96.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
