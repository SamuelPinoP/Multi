package com.example.multi

import com.example.multi.util.occursOn
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class EventUtilsTest {
    @Test
    fun occursOn_everyOtherWednesday_selectsAlternateWeeks() {
        val event = Event(title = "Test", description = "", date = "Every other Wednesday")
        val week1 = LocalDate.parse("2024-05-01") // week 18, Wednesday
        val week2 = week1.plusWeeks(1)
        val week3 = week1.plusWeeks(2)
        assertTrue(event.occursOn(week1))
        assertFalse(event.occursOn(week2))
        assertTrue(event.occursOn(week3))
    }
}
