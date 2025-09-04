package com.example.multi

import org.junit.Assert.assertEquals
import org.junit.Test

class OverageCalculationTest {
    @Test
    fun overage_is_zero_when_under_target() {
        assertEquals(0, calculateOverage(2, 5))
    }

    @Test
    fun overage_positive_when_over_target() {
        assertEquals(1, calculateOverage(4, 3))
    }

    @Test
    fun overage_clamped_to_twenty() {
        assertEquals(20, calculateOverage(50, 3))
    }
}
