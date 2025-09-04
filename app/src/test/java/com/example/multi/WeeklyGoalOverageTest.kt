package com.example.multi

import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyGoalOverageTest {
    private fun overage(completed: Int, target: Int): Int {
        return (completed - target).coerceAtLeast(0).coerceAtMost(20)
    }

    @Test
    fun overageWithinRange() {
        assertEquals(1, overage(4, 3))
        assertEquals(20, overage(24, 3))
        assertEquals(0, overage(2, 5))
    }
}

