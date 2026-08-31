package com.example.multi.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

/**
 * Motion tokens. Multi's personality is "motion-forward" but never sluggish —
 * short durations, an emphasised deceleration curve, and a touch of spring on
 * press feedback (springs live at the call site via `Spring.*`).
 */
object MultiMotion {
    /** Micro feedback: press states, checkbox ticks, chip toggles. */
    const val DurationFast = 120

    /** Standard: content fades, expand/collapse, list item enter. */
    const val DurationMedium = 240

    /** Deliberate: screen-level transitions, dialog entrances, hero moves. */
    const val DurationSlow = 400

    /** Ambient loops: gradient drift, shimmer. */
    const val DurationAmbient = 9000

    /** Material's standard emphasised-decelerate curve. */
    val EasingStandard: Easing = FastOutSlowInEasing

    /** Emphasised curve for hero motion — slow start, confident finish. */
    val EasingEmphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
