package com.example.multi

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.time.LocalDate
import java.util.HashSet

private class EventDecorator(
    private val color: Int,
    dates: Collection<LocalDate>
) : DayViewDecorator {

    private val dates: HashSet<CalendarDay> = dates
        .map { CalendarDay.from(it) }
        .toCollection(HashSet())

    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, color))
    }
}

@Composable
fun CalendarView(
    events: List<LocalDate> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                if (events.isNotEmpty()) {
                    addDecorator(EventDecorator(Color.RED, events))
                }
            }
        },
        modifier = modifier,
        update = { view ->
            view.removeDecorators()
            if (events.isNotEmpty()) {
                view.addDecorator(EventDecorator(Color.RED, events))
            }
        }
    )
}
