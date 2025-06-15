package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.time.LocalDate

/**
 * Calendar view backed by the MaterialCalendarView widget.
 *
 * @param events Dates that should display a dot indicator.
 */
@Composable
fun CalendarView(events: Set<LocalDate> = emptySet()) {
    val eventDays = events.map { CalendarDay.from(it) }.toSet()
    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                setSelectedDate(CalendarDay.today())
            }
        },
        update = { view ->
            view.removeDecorators()
            if (eventDays.isNotEmpty()) {
                view.addDecorator(EventDecorator(eventDays))
            }
        }
    )
}

private class EventDecorator(
    private val dates: Set<CalendarDay>,
    private val color: Int = Color.Red.toArgb()
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(6f, color))
    }
}
