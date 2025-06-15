package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.time.DayOfWeek
import java.time.LocalDate

/** Returns the zero-based offset used by the calendar grid for this day. */
internal fun DayOfWeek.toCalendarOffset(): Int = (this.value + 6) % 7

/**
 * Displays a [MaterialCalendarView] using Jetpack Compose. The calendar
 * highlights any days present in [eventDates] using a small colored dot.
 */
@Composable
fun CalendarView(
    date: LocalDate = LocalDate.now(),
    eventDates: Set<LocalDate> = emptySet()
) {
    val context = LocalContext.current
    val dates = remember(eventDates) {
        eventDates.map { CalendarDay.from(it) }.toSet()
    }

    AndroidView(factory = {
        MaterialCalendarView(context).apply {
            selectedDate = CalendarDay.from(date)
            if (dates.isNotEmpty()) {
                addDecorator(EventDecorator(dates))
            }
        }
    }, update = { view ->
        view.selectedDate = CalendarDay.from(date)
    })
}

private class EventDecorator(
    private val dates: Set<CalendarDay>
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay?): Boolean = day != null && dates.contains(day)

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan())
    }
}
