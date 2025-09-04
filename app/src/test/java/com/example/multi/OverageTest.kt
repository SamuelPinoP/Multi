package com.example.multi

import org.junit.Assert.assertEquals
import org.junit.Test

class OverageTest {
    @Test
    fun overageCalculation() {
        assertEquals(1, calculateOverage(4, 3))
        assertEquals(20, calculateOverage(24, 3))
        assertEquals(20, calculateOverage(23, 3))
        assertEquals(0, calculateOverage(2, 3))
    }
}
