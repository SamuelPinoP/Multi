package com.example.multi

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.multi.data.EventDatabase
import java.time.LocalDate

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val context = LocalContext.current
            val eventDatesState = remember { mutableStateOf(setOf<LocalDate>()) }

            LaunchedEffect(Unit) {
                val dao = EventDatabase.getInstance(context).eventDao()
                val stored = withContext(Dispatchers.IO) { dao.getEvents() }
                val dates = stored.mapNotNull { entity ->
                    entity.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                }.toSet()
                eventDatesState.value = dates
            }

            CalendarView(eventDates = eventDatesState.value)
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
