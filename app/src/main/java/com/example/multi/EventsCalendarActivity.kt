package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.multi.data.EventDatabase

class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun SegmentContent() {
        EventsCalendarView()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EventsCalendarView() {
    val context = LocalContext.current
    val eventDates = remember { mutableStateListOf<CalendarDay>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        val parsed = stored.mapNotNull { event ->
            event.date?.let {
                runCatching { LocalDate.parse(it, formatter) }.getOrNull()
            }
        }
        eventDates.clear()
        eventDates.addAll(parsed.map { CalendarDay.from(it) })
    }

    AndroidView(factory = { ctx ->
        MaterialCalendarView(ctx)
    }, update = { view ->
        view.removeDecorators()
        if (eventDates.isNotEmpty()) {
            view.addDecorator(object : DayViewDecorator {
                override fun shouldDecorate(day: CalendarDay): Boolean {
                    return eventDates.contains(day)
                }

                override fun decorate(facade: DayViewFacade) {
                    facade.addSpan(DotSpan(8f, 0xFF4CAF50.toInt()))
                }
            })
        }
    })
}
