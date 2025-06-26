package com.example.multi

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.Locale

/** Activity displaying a simple Kizitonwose calendar. */
class EventsCalendarActivity : SegmentActivity("Event Calendar") {
    @Composable
    override fun SegmentContent() {
        EventsCalendarScreen()
    }
}

private class DayViewContainer(val textView: TextView) : ViewContainer(textView) {
    lateinit var day: CalendarDay
}

private class MonthHeaderContainer(val textView: TextView) : ViewContainer(textView)

@Composable
private fun EventsCalendarScreen() {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            CalendarView(ctx).apply {
                val currentMonth = YearMonth.now()
                val startMonth = currentMonth.minusMonths(12)
                val endMonth = currentMonth.plusMonths(12)
                val daysOfWeek = DayOfWeek.values().toList()
                dayBinder = object : DayBinder<DayViewContainer> {
                    override fun create(view: android.view.View) = DayViewContainer(TextView(ctx))
                    override fun bind(container: DayViewContainer, data: CalendarDay) {
                        container.textView.text = data.date.dayOfMonth.toString()
                    }
                }
                monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderContainer> {
                    override fun create(view: android.view.View) = MonthHeaderContainer(TextView(ctx))
                    override fun bind(container: MonthHeaderContainer, data: CalendarMonth) {
                        container.textView.text = data.yearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()) + " " + data.yearMonth.year
                    }
                }
                setup(startMonth, endMonth, daysOfWeek.first())
                scrollToMonth(currentMonth)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}


