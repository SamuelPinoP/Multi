package com.example.multi

import com.example.multi.util.computeOverage
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyGoalUtilsTest {
    @Test
    fun computeOverage_basicCases() {
        assertEquals(0, computeOverage(2, 3))
        assertEquals(1, computeOverage(4, 3))
        assertEquals(20, computeOverage(24, 3))
    }
}
