package com.example.multi

fun calculateOverage(completed: Int, target: Int): Int {
    return (completed - target).coerceAtLeast(0).coerceAtMost(20)
}
