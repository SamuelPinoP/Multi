package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.multi.data.EventDatabase
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay
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

@Composable
fun EventsCalendarScreen() {
    val context = LocalContext.current
    val eventDates = remember { mutableStateListOf<LocalDate>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        eventDates.clear()
        for (event in stored) {
            val dateStr = event.date
            if (dateStr != null) {
                try {
                    eventDates.add(LocalDate.parse(dateStr))
                } catch (e: Exception) {
                    // ignore unparsable dates
                }
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                selectionMode = MaterialCalendarView.SELECTION_MODE_NONE
            }
        },
        update = { view ->
            val days = eventDates.map {
                CalendarDay.from(it.year, it.monthValue, it.dayOfMonth)
            }.toSet()
            view.removeDecorators()
            view.addDecorator(object : DayViewDecorator {
                override fun shouldDecorate(day: CalendarDay): Boolean {
                    return days.contains(day)
                }

                override fun decorate(facade: DayViewFacade) {
                    facade.addSpan(DotSpan(8f, android.graphics.Color.GREEN))
                }
            })
        }
    )
}
