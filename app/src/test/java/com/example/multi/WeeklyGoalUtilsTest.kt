package com.example.multi

import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyGoalUtilsTest {
    @Test
    fun overageCalculation() {
        assertEquals(0, overageCount(3, 3))
        assertEquals(1, overageCount(4, 3))
        assertEquals(20, overageCount(24, 3))
        assertEquals(0, overageCount(2, 3))
    }
}
