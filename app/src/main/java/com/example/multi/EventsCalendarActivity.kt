package com.example.multi

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel

class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val dates = remember { mutableStateListOf<LocalDate>() }

        LaunchedEffect(Unit) {
            val dao = EventDatabase.getInstance(context).eventDao()
            val stored = withContext(Dispatchers.IO) { dao.getEvents() }
            dates.clear()
            stored.mapNotNull { it.toModel().date?.let { d -> runCatching { LocalDate.parse(d) }.getOrNull() } }
                .forEach { dates.add(it) }
        }

        AndroidView(factory = { ctx ->
            MaterialCalendarView(ctx).apply {
                setOnDateChangedListener { _, date, _ ->
                    val selected = LocalDate.of(date.year, date.month, date.day)
                    val intent = Intent(ctx, EventsActivity::class.java)
                    intent.putExtra(EXTRA_DATE, selected.toString())
                    ctx.startActivity(intent)
                }
            }
        }) { view ->
            val marked = dates.map { CalendarDay.from(it.year, it.monthValue, it.dayOfMonth) }.toSet()
            view.removeDecorators()
            if (marked.isNotEmpty()) {
                view.addDecorator(object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay) = marked.contains(day)
                    override fun decorate(facade: DayViewFacade) {
                        facade.addSpan(DotSpan(8f, 0xFF2E7D32.toInt()))
                    }
                })
            }
        }
    }
}
