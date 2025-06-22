package com.example.multi

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.multi.data.EventDatabase
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsCalendarActivity : SegmentActivity("Event Calendar") {
    @Composable
    override fun SegmentContent() {
        EventsCalendarScreen()
    }
}

@Composable
fun EventsCalendarScreen() {
    val context = LocalContext.current
    val dates = remember { mutableStateListOf<CalendarDay>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val events = withContext(Dispatchers.IO) { dao.getEvents() }
        dates.clear()
        events.forEach { event ->
            val d = event.date
            if (d != null) {
                try {
                    val ld = LocalDate.parse(d)
                    dates.add(CalendarDay.from(ld.year, ld.monthValue, ld.dayOfMonth))
                } catch (_: Exception) {
                }
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                setOnDateChangedListener { _, date, _ ->
                    val ld = LocalDate.of(date.year, date.month, date.day)
                    val intent = Intent(ctx, EventsActivity::class.java)
                    intent.putExtra(EXTRA_DATE, ld.toString())
                    ctx.startActivity(intent)
                }
            }
        },
        update = { view ->
            view.removeDecorators()
            if (dates.isNotEmpty()) {
                view.addDecorator(EventDecorator(dates.toSet(), android.graphics.Color.parseColor("#4CAF50")))
            }
        }
    )
}

class EventDecorator(private val dates: Set<CalendarDay>, private val color: Int) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, color))
    }
}

