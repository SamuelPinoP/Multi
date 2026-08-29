package com.example.multi

import com.example.multi.util.toDateString
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DateUtilsTest {

    private fun epochMillisFor(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 1

    @Test
    fun formatsAsMonthDayYear() {
        val millis = epochMillisFor(LocalDate.of(2024, 3, 7))
        assertEquals("3/7/2024", millis.toDateString())
    }

    @Test
    fun doesNotZeroPadMonthOrDay() {
        val millis = epochMillisFor(LocalDate.of(2025, 12, 25))
        assertEquals("12/25/2025", millis.toDateString())
    }
}
