package com.example.multi

import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import android.graphics.Color

class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            EventsCalendarScreen()
        } else {
            androidx.compose.material3.Text("Calendar requires Android O or higher")
        }
    }
}

@Composable
fun EventsCalendarScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val eventDates = remember { mutableStateListOf<LocalDate>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        val dates = stored.mapNotNull { it.date }
            .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        eventDates.clear()
        eventDates.addAll(dates)
    }

    AndroidView(
        factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                setOnDateChangedListener { _, day, _ ->
                    val dateStr = String.format("%04d-%02d-%02d", day.year, day.month, day.day)
                    val intent = Intent(ctx, EventsActivity::class.java)
                    intent.putExtra(EXTRA_DATE, dateStr)
                    ctx.startActivity(intent)
                }
            }
        },
        update = { view ->
            val days = eventDates.map {
                CalendarDay.from(it.year, it.monthValue, it.dayOfMonth)
            }.toSet()
            view.removeDecorators()
            if (days.isNotEmpty()) {
                view.addDecorator(object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay): Boolean = days.contains(day)
                    override fun decorate(facade: DayViewFacade) {
                        facade.addSpan(DotSpan(8f, Color.GREEN))
                    }
                })
            }
        },
        modifier = modifier
    )
}
