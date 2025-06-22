package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

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
    val events = remember { mutableStateListOf<Event>() }

    LaunchedEffect(Unit) {
        scope.launch {
            val dao = EventDatabase.getInstance(context).eventDao()
            val stored = withContext(Dispatchers.IO) { dao.getEvents() }
            events.clear()
            events.addAll(stored.map { it.toModel() })
        }
    }

    val eventDates = events.mapNotNull { e ->
        e.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }.toSet()

    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                selectionMode = MaterialCalendarView.SELECTION_MODE_NONE
            }
        },
        update = { view ->
            view.removeDecorators()
            if (eventDates.isNotEmpty()) {
                val days = eventDates.map { date ->
                    CalendarDay.from(date.year, date.monthValue, date.dayOfMonth)
                }.toSet()
                view.addDecorator(object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay): Boolean {
                        return days.contains(day)
                    }

                    override fun decorate(facade: DayViewFacade) {
                        facade.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.GREEN))
                    }
                })
            }
        }
    )
}

