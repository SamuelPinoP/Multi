package com.example.multi.util

/** Calculates over-completion count for weekly goals, clamped to 0..20. */
fun computeOverage(completed: Int, target: Int): Int {
    return (completed - target).coerceAtLeast(0).coerceAtMost(20)
}
