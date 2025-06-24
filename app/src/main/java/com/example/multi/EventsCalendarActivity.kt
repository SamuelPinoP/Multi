package com.example.multi

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.multi.data.EventDatabase
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            EventsCalendarScreen()
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}

@Composable
private fun EventsCalendarScreen() {
    val context = LocalContext.current
    var dates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        dates = stored.mapNotNull { event ->
            runCatching { LocalDate.parse(event.date) }.getOrNull()
        }.toSet()
    }

    EventsCalendarView(dates)
}

@Composable
private fun EventsCalendarView(eventDates: Set<LocalDate>) {
    val currentMonth = remember { YearMonth.now() }
    val daysOfWeek = remember { daysOfWeek() }

    VerticalCalendar(
        month = currentMonth,
        daysOfWeek = daysOfWeek,
        modifier = Modifier.fillMaxWidth(),
        dayContent = { day ->
            val isEvent = eventDates.contains(day.date)
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .background(
                        color = if (isEvent) Color.Green else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = if (isEvent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
