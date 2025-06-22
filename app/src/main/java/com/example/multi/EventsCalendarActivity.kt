package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import java.time.LocalDate
import kotlinx.coroutines.launch

/** Activity showing a calendar with event dates highlighted. */
class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        EventsCalendarScreen()
    }
}

@Composable
private fun EventsCalendarScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dates = remember { mutableStateListOf<CalendarDay>() }
    var calendarView by remember { mutableStateOf<MaterialCalendarView?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            val dao = EventDatabase.getInstance(context).eventDao()
            val stored = withContext(Dispatchers.IO) { dao.getEvents() }
            dates.clear()
            stored.mapNotNull { it.toModel().date }.forEach { dateStr ->
                runCatching { LocalDate.parse(dateStr) }.getOrNull()?.let { d ->
                    dates.add(CalendarDay.from(d.year, d.monthValue - 1, d.dayOfMonth))
                }
            }
            calendarView?.let { view ->
                view.removeDecorators()
                if (dates.isNotEmpty()) {
                    view.addDecorator(EventDecorator(dates.toSet()))
                }
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx).also { calendarView = it }
        },
        update = { view ->
            view.removeDecorators()
            if (dates.isNotEmpty()) {
                view.addDecorator(EventDecorator(dates.toSet()))
            }
        }
    )
}

private class EventDecorator(
    private val dates: Set<CalendarDay>
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, android.graphics.Color.GREEN))
    }
}
