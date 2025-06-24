package com.example.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Display a calendar grid marking the given event [dates] in green.
 */
@Composable
fun EventsCalendarView(dates: Set<LocalDate>) {
    val currentMonth = remember { YearMonth.now() }
    val daysOfWeek = remember { daysOfWeek() }

    VerticalCalendar(
        month = currentMonth,
        daysOfWeek = daysOfWeek,
        modifier = Modifier.fillMaxWidth(),
        dayContent = { day ->
            val isEvent = dates.contains(day.date)
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .background(
                        if (isEvent) Color.Green else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = day.date.dayOfMonth.toString())
            }
        }
    )
}
