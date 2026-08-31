package com.example.multi.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Multi shape system — generously rounded, in keeping with the "tactile,
 * rounded" personality. These map onto the Material 3 [Shapes] slots so that
 * every component (cards, sheets, menus, text fields, chips) picks them up
 * automatically.
 *
 *  | slot        | radius | typical use                        |
 *  |-------------|--------|------------------------------------|
 *  | extraSmall  |  8 dp  | chips, snackbars, small buttons     |
 *  | small       | 12 dp  | text fields, menus                  |
 *  | medium      | 16 dp  | list cards, dialogs surfaces        |
 *  | large       | 22 dp  | hero cards, bottom sheets           |
 *  | extraLarge  | 28 dp  | full-bleed feature surfaces         |
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
