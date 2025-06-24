package com.example.multi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.day.Day
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel

@Composable
fun EventsCalendarScreen() {
    val context = LocalContext.current
    val events = remember { mutableStateListOf<Event>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        events.clear()
        events.addAll(stored.map { it.toModel() })
    }

    val eventDates = remember(events) {
        events.mapNotNull { it.date?.let { d -> parseDate(d) } }.toSet()
    }

    EventsCalendarView(eventDates)
}

private fun parseDate(text: String): LocalDate? = try {
    LocalDate.parse(text)
} catch (e: Exception) { null }

@Composable
fun EventsCalendarView(eventDates: Set<LocalDate>) {
    val currentMonth = remember { YearMonth.now() }
    val daysOfWeek = remember { daysOfWeek() }
    val state = rememberCalendarState(currentMonth)

    VerticalCalendar(
        state = state,
        monthHeader = {},
        daysOfWeek = daysOfWeek,
        modifier = Modifier.fillMaxWidth(),
        dayContent = { day: Day ->
            val isEvent = eventDates.contains(day.date)
            Box(
                modifier = Modifier
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
