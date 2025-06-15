package com.example.multi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.time.LocalDate
import java.util.HashSet

private class EventDecorator(
    private val dates: HashSet<CalendarDay>,
    private val color: Int
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, color))
    }
}

@Composable
fun CalendarView(
    events: Map<LocalDate, String> = mapOf(
        LocalDate.now() to "Sample Event",
        LocalDate.now().plusDays(2) to "Another Event"
    )
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val eventDates = remember(events) {
        events.keys.map { CalendarDay.from(it.year, it.monthValue, it.dayOfMonth) }.toHashSet()
    }
    val color = MaterialTheme.colorScheme.primary.toArgb()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AndroidView(
            factory = { context ->
                MaterialCalendarView(context).apply {
                    addDecorators(EventDecorator(eventDates, color))
                    setOnDateChangedListener { _, date, _ ->
                        selectedDate = LocalDate.of(date.year, date.month, date.day)
                    }
                }
            },
            update = { view ->
                view.setOnDateChangedListener { _, date, _ ->
                    selectedDate = LocalDate.of(date.year, date.month, date.day)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        selectedDate?.let { date ->
            Text(
                text = events[date] ?: "No events",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
