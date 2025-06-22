package com.example.multi

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.multi.data.EventDatabase
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        EventsCalendarScreen()
    }
}

private class EventDecorator(private val dates: Set<CalendarDay>) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, Color.GREEN))
    }
}

@Composable
private fun EventsCalendarScreen() {
    val context = LocalContext.current
    val eventDays = remember { mutableStateListOf<CalendarDay>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val events = withContext(Dispatchers.IO) { dao.getEvents() }
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        eventDays.clear()
        events.forEach { ev ->
            val str = ev.date
            if (str != null && str.length >= 10) {
                try {
                    val date = LocalDate.parse(str.substring(0, 10), fmt)
                    eventDays.add(CalendarDay.from(date.year, date.monthValue, date.dayOfMonth))
                } catch (_: Exception) {
                }
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                addDecorator(EventDecorator(eventDays.toSet()))
            }
        },
        update = { view ->
            view.removeDecorators()
            view.addDecorator(EventDecorator(eventDays.toSet()))
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}
