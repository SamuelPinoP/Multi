package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.Text
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
            CalendarScreen()
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}

@Composable
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.O)
private fun CalendarScreen() {
    val context = LocalContext.current
    var highlightDates by remember { mutableStateOf(setOf<LocalDate>()) }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val events = withContext(Dispatchers.IO) { dao.getEvents() }
        highlightDates = events.mapNotNull { event ->
            event.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        }.toSet()
    }

    CalendarView(highlightDates = highlightDates)
}
