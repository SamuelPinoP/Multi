package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import java.time.LocalDate
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

    CalendarView(eventDates = eventDates)
}

private fun parseDate(text: String): LocalDate? = try {
    LocalDate.parse(text)
} catch (e: Exception) { null }
}
