package com.example.multi

/**
 * Calculate how many completions exceed the weekly target.
 */
fun overageCount(completed: Int, target: Int): Int {
    return (completed - target).coerceAtLeast(0).coerceAtMost(20)
}

