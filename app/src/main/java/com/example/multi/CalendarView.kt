package com.example.multi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Returns the zero-based offset used by the calendar grid for this day. */
internal fun DayOfWeek.toCalendarOffset(): Int = (this.value + 6) % 7

/**
 * Simple visual calendar for the given [date]. Displays the month, year,
 * days of the week and the days of the month in a grid. No interaction
 * is provided.
 */
@Composable
fun CalendarView(date: LocalDate = LocalDate.now()) {
    val yearMonth = YearMonth.from(date)
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val daysOfWeek = DayOfWeek.values()
    val locale = Locale.getDefault()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, locale)} ${yearMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            for (day in daysOfWeek) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        val firstDayOffset = firstDayOfMonth.dayOfWeek.toCalendarOffset()
        var currentDay = 1
        val totalCells = firstDayOffset + daysInMonth
        val rows = (totalCells + 6) / 7
        Column {
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        if (cellIndex < firstDayOffset || currentDay > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentDay.toString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            currentDay++
                        }
                    }
                }
            }
        }
    }
}
