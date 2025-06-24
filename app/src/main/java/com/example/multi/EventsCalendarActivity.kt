package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.multi.data.EventDatabase
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

/**
 * Activity showing a calendar with event days highlighted.
 */
class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        var eventDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }

        LaunchedEffect(Unit) {
            val dao = EventDatabase.getInstance(context).eventDao()
            val stored = withContext(Dispatchers.IO) { dao.getEvents() }
            eventDates = stored.mapNotNull { entity ->
                entity.date?.let {
                    try { LocalDate.parse(it) } catch (_: Exception) { null }
                }
            }.toSet()
        }

        EventsCalendarView(eventDates)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventsCalendarView(eventDates: Set<LocalDate>) {
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
                        if (isEvent) Color.Green else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = day.date.dayOfMonth.toString())
            }
        }
    )
}
