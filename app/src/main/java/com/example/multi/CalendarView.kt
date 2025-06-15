package com.example.multi

import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.time.LocalDate

/**
 * Displays a [MaterialCalendarView] with simple event support.
 * Dates present in the [events] map will display a marker. Selecting a date
 * shows its description below the calendar.
 */
@Composable
fun CalendarView(events: Map<CalendarDay, String> = sampleEvents()) {
    var selected by remember { mutableStateOf<String?>(null) }

    Column {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                MaterialCalendarView(ctx).apply {
                    addDecorator(EventDecorator(events.keys))
                    setOnDateChangedListener { _, date, _ ->
                        selected = events[date]
                    }
                }
            }
        )
        selected?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

/** Simple sample data used if no events are provided. */
private fun sampleEvents(): Map<CalendarDay, String> {
    val today = LocalDate.now()
    return mapOf(
        CalendarDay.from(today) to "Today's event",
        CalendarDay.from(today.plusDays(2)) to "Event in two days"
    )
}

private class EventDecorator(
    private val dates: Set<CalendarDay>,
    private val color: Int = Color.RED
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, color))
    }
}
