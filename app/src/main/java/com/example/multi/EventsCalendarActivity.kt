package com.example.multi

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate

class EventsCalendarActivity : SegmentActivity("Event Calendar") {
    @Composable
    override fun SegmentContent() {
        EventsCalendarScreen()
    }
}

@Composable
private fun EventsCalendarScreen() {
    val context = LocalContext.current
    val eventDays = remember { mutableStateListOf<CalendarDay>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        eventDays.clear()
        for (ev in stored.map { it.toModel() }) {
            val dateStr = ev.date ?: continue
            try {
                val local = LocalDate.parse(dateStr)
                eventDays.add(CalendarDay.from(local))
            } catch (_: Exception) {
            }
        }
    }

    AndroidView(
        modifier = Modifier,
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                addDecorator(object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay): Boolean {
                        return eventDays.contains(day)
                    }

                    override fun decorate(facade: DayViewFacade) {
                        facade.setBackgroundDrawable(ColorDrawable(Color.GREEN))
                    }
                })
            }
        },
        update = { view ->
            view.invalidateDecorators()
        }
    )
}

