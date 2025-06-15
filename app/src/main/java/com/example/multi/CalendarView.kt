package com.example.multi

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.time.DayOfWeek
import java.time.LocalDate

/** Returns the zero-based offset used by the calendar grid for this day. */
internal fun DayOfWeek.toCalendarOffset(): Int = (this.value + 6) % 7

/**
 * Calendar implementation backed by [MaterialCalendarView].
 *
 * [events] maps each date to a list of event descriptions. Dates with events
 * are highlighted with a dot indicator.
 */
@Composable
fun CalendarView(
    events: Map<LocalDate, List<String>> = emptyMap(),
    date: LocalDate = LocalDate.now()
) {
    AndroidView(
        factory = { context ->
            MaterialCalendarView(context).apply {
                selectedDate = CalendarDay.from(date)
                firstDayOfWeek = DayOfWeek.MONDAY
                if (events.isNotEmpty()) {
                    addDecorator(EventDecorator(events.keys))
                }
            }
        },
        update = { view ->
            view.removeDecorators()
            if (events.isNotEmpty()) {
                view.addDecorator(EventDecorator(events.keys))
            }
        }
    )
}

private class EventDecorator(dates: Collection<LocalDate>) : DayViewDecorator {
    private val days = dates.map { CalendarDay.from(it) }.toSet()

    override fun shouldDecorate(day: CalendarDay): Boolean = days.contains(day)

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, Color.RED))
    }
}
