package com.example.multi.util

fun calculateOverage(completed: Int, target: Int): Int =
    (completed - target).coerceAtLeast(0).coerceAtMost(20)
