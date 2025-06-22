package com.example.multi

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.github.prolificinteractive.materialcalendarview.CalendarDay
import com.github.prolificinteractive.materialcalendarview.DayViewDecorator
import com.github.prolificinteractive.materialcalendarview.DayViewFacade
import com.github.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.multi.data.EventDatabase
import java.time.LocalDate

private class EventDecorator(
    private val dates: Set<CalendarDay>,
    private val color: Int
) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, color))
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun MaterialCalendarScreen() {
    val context = LocalContext.current
    val eventDays = remember { mutableStateOf<Set<CalendarDay>>(emptySet()) }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        val dates = stored.mapNotNull { it.date?.let { d ->
            val ld = LocalDate.parse(d)
            CalendarDay.from(ld.year, ld.monthValue, ld.dayOfMonth)
        } }.toSet()
        eventDays.value = dates
    }

    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                setOnDateChangedListener { _, day, _ ->
                    val ld = LocalDate.of(day.year, day.month, day.day)
                    val intent = Intent(ctx, EventsActivity::class.java)
                    intent.putExtra(EXTRA_DATE, ld.toString())
                    ctx.startActivity(intent)
                }
            }
        },
        update = { view ->
            view.removeDecorators()
            if (eventDays.value.isNotEmpty()) {
                view.addDecorator(EventDecorator(eventDays.value, android.graphics.Color.GREEN))
            }
        }
    )
}
