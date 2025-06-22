package com.example.multi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel

class CalendarHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_home)
        val calendar = findViewById<MaterialCalendarView>(R.id.calendarView)

        CoroutineScope(Dispatchers.Main).launch {
            val events = withContext(Dispatchers.IO) {
                val dao = EventDatabase.getInstance(this@CalendarHomeActivity).eventDao()
                dao.getEvents().map { it.toModel() }
            }
            val eventDates = events.mapNotNull { it.date?.let { d -> LocalDate.parse(d) } }
            val days = eventDates.map { CalendarDay.from(it.year, it.monthValue, it.dayOfMonth) }.toSet()
            calendar.addDecorator(EventDecorator(days))
            calendar.setOnDateChangedListener { _, date, _ ->
                val selected = LocalDate.of(date.year, date.month, date.day)
                if (eventDates.contains(selected)) {
                    EventDialogFragment.newInstance(selected.toString()).show(supportFragmentManager, "event")
                }
            }
        }
    }

    class EventDecorator(private val dates: Set<CalendarDay>) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)
        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(8f, Color.GREEN))
        }
    }
}
