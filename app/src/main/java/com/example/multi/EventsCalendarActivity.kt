package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import java.time.YearMonth
import java.time.DayOfWeek

class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun SegmentContent() {
        KizitonwoseCalendar()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun KizitonwoseCalendar() {
    val currentMonth = YearMonth.now()
    val state = rememberCalendarState(
        startMonth = currentMonth.minusMonths(12),
        endMonth = currentMonth.plusMonths(12),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY,
    )
    HorizontalCalendar(state = state, dayContent = { day ->
        Text(day.date.dayOfMonth.toString())
    })
}
