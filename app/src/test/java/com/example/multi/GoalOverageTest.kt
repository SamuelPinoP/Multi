package com.example.multi

import com.example.multi.util.calculateOverage
import org.junit.Assert.assertEquals
import org.junit.Test

class GoalOverageTest {
    @Test
    fun overageCalculation() {
        assertEquals(0, calculateOverage(3, 3))
        assertEquals(1, calculateOverage(4, 3))
        assertEquals(20, calculateOverage(24, 3))
        assertEquals(0, calculateOverage(2, 3))
    }
}
