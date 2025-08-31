package com.example.multi

import java.time.DayOfWeek
import org.junit.Test
import org.junit.Assert.assertEquals

class CalendarViewTest {
    @Test
    fun dayOfWeek_toCalendarOffset_returnsExpectedValues() {
        assertEquals(0, DayOfWeek.SUNDAY.toCalendarOffset())
        assertEquals(1, DayOfWeek.MONDAY.toCalendarOffset())
        assertEquals(2, DayOfWeek.TUESDAY.toCalendarOffset())
        assertEquals(3, DayOfWeek.WEDNESDAY.toCalendarOffset())
        assertEquals(4, DayOfWeek.THURSDAY.toCalendarOffset())
        assertEquals(5, DayOfWeek.FRIDAY.toCalendarOffset())
        assertEquals(6, DayOfWeek.SATURDAY.toCalendarOffset())
    }
}
