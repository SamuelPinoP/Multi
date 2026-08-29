package com.example.multi

import com.example.multi.util.occursOn
import com.example.multi.util.recurringDays
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
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

    @Test
    fun occursOn_exactIsoDate_matchesOnlyThatDay() {
        val event = Event(title = "Dentist", description = "", date = "2024-06-15")
        assertTrue(event.occursOn(LocalDate.parse("2024-06-15")))
        assertFalse(event.occursOn(LocalDate.parse("2024-06-16")))
    }

    @Test
    fun occursOn_nullDate_neverOccurs() {
        val event = Event(title = "Someday", description = "", date = null)
        assertFalse(event.occursOn(LocalDate.parse("2024-06-15")))
    }

    @Test
    fun occursOn_weeklyByDayName_matchesEveryMatchingWeekday() {
        val event = Event(title = "Standup", description = "", date = "Every Monday and Thursday")
        assertTrue(event.occursOn(LocalDate.parse("2024-06-03")))  // Monday
        assertTrue(event.occursOn(LocalDate.parse("2024-06-06")))  // Thursday
        assertFalse(event.occursOn(LocalDate.parse("2024-06-04"))) // Tuesday
    }

    @Test
    fun recurringDays_detectsAbbreviatedAndFullNames() {
        assertEquals(setOf(DayOfWeek.TUESDAY), "every tue".recurringDays())
        assertEquals(
            setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            "Weekends: Saturday & Sunday".recurringDays(),
        )
    }

    @Test
    fun recurringDays_returnsNullForPlainDateOrNoDayName() {
        assertNull("2024-06-15".recurringDays())
        assertNull("Lunch with mom".recurringDays())
        assertNull((null as String?).recurringDays())
    }
}
