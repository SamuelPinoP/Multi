package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import android.graphics.Color

class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        EventsCalendarScreen()
    }
}

@Composable
private fun EventsCalendarScreen() {
    val context = LocalContext.current
    val eventDatesState = remember { mutableStateOf<Set<LocalDate>>(emptySet()) }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        val set = stored.mapNotNull { it.date }
            .filter { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
            .map { LocalDate.parse(it) }
            .toSet()
        eventDatesState.value = set
    }

    AndroidView(
        factory = { ctx -> MaterialCalendarView(ctx) },
        update = { view ->
            view.removeDecorators()
            view.addDecorator(EventDayDecorator(eventDatesState.value))
        }
    )
}

private class EventDayDecorator(private val dates: Set<LocalDate>) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        val date = LocalDate.of(day.year, day.month, day.day)
        return dates.contains(date)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, Color.GREEN))
    }
}
