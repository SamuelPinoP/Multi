package com.example.multi

import android.graphics.Color
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.multi.data.EventDatabase
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private class EventDecorator(
    private val color: Int,
    private val dates: Collection<CalendarDay>
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, color))
    }
}

@Composable
fun MaterialCalendar() {
    val context = LocalContext.current
    val eventDays = remember { mutableStateListOf<CalendarDay>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val events = withContext(Dispatchers.IO) { dao.getEvents() }
        val days = events.mapNotNull { event ->
            event.date?.let { dateStr ->
                runCatching { LocalDate.parse(dateStr) }.getOrNull()?.let { d ->
                    CalendarDay.from(d.year, d.monthValue, d.dayOfMonth)
                }
            }
        }
        eventDays.clear()
        eventDays.addAll(days)
    }

    AndroidView(
        factory = { ctx ->
            LayoutInflater.from(ctx).inflate(R.layout.material_calendar, null) as MaterialCalendarView
        },
        update = { view ->
            view.removeDecorators()
            view.addDecorator(EventDecorator(Color.parseColor("#32CD32"), eventDays))
        }
    )
}
