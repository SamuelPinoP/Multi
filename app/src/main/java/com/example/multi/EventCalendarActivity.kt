package com.example.multi

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class EventCalendarActivity : SegmentActivity("Event Calendar") {
    @Composable
    override fun SegmentContent() {
        EventCalendarScreen()
    }
}

@Composable
private fun EventCalendarScreen() {
    val context = LocalContext.current
    val eventDays = remember { mutableStateListOf<CalendarDay>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        eventDays.clear()
        eventDays.addAll(
            stored.mapNotNull { it.toModel().date }
                .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
                .map { CalendarDay.from(it.year, it.monthValue - 1, it.dayOfMonth) }
        )
    }

    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                addDecorator(EventDecorator(eventDays))
                setOnDateChangedListener { _, date, _ ->
                    val local = LocalDate.of(date.year, date.month + 1, date.day)
                    val intent = Intent(ctx, EventsActivity::class.java)
                    intent.putExtra(EXTRA_DATE, local.toString())
                    ctx.startActivity(intent)
                }
            }
        },
        update = { view ->
            view.removeDecorators()
            view.addDecorator(EventDecorator(eventDays))
        }
    )
}

private class EventDecorator(
    private val dates: Collection<CalendarDay>
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, android.graphics.Color.GREEN))
    }
}
