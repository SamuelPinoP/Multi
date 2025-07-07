package com.example.multi

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class EventTest {
    @Test
    fun occursOn_singleDate() {
        val e = Event(title = "A", description = "B", date = "2024-01-05")
        assertTrue(e.occursOn(LocalDate.parse("2024-01-05")))
        assertFalse(e.occursOn(LocalDate.parse("2024-01-06")))
    }

    @Test
    fun occursOn_everyTuesday() {
        val e = Event(title = "A", description = "B", date = "Every Tuesday")
        assertTrue(e.occursOn(LocalDate.parse("2024-01-02"))) // Tuesday
        assertFalse(e.occursOn(LocalDate.parse("2024-01-03"))) // Wednesday
    }

    @Test
    fun occursOn_everyOtherMonday() {
        val e = Event(title = "A", description = "B", date = "Every other Monday")
        // 2024-01-01 is week 1 -> odd, should be false
        assertFalse(e.occursOn(LocalDate.parse("2024-01-01")))
        // 2024-01-08 is week 2 -> even, should be true
        assertTrue(e.occursOn(LocalDate.parse("2024-01-08")))
    }
}
