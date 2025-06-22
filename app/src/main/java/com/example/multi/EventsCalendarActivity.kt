package com.example.multi

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        EventsCalendarScreen()
    }
}

private class EventDecorator(
    private val dates: Set<CalendarDay>
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, Color.GREEN))
    }
}

@Composable
private fun EventsCalendarScreen() {
    val context = LocalContext.current
    var eventDays by remember { mutableStateOf<Set<CalendarDay>>(emptySet()) }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        val days = stored.mapNotNull { entity ->
            val dateStr = entity.toModel().date
            try {
                dateStr?.let { LocalDate.parse(it) }?.let {
                    CalendarDay.from(it.year, it.monthValue, it.dayOfMonth)
                }
            } catch (_: Exception) {
                null
            }
        }.toSet()
        eventDays = days
    }

    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx)
        },
        modifier = Modifier,
        update = { view ->
            view.removeDecorators()
            if (eventDays.isNotEmpty()) {
                view.addDecorator(EventDecorator(eventDays))
            }
        }
    )
}

