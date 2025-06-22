package com.example.multi

import androidx.compose.runtime.*
import androidx.compose.material.Text
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val context = LocalContext.current
            var dates by remember { mutableStateOf(setOf<LocalDate>()) }
            LaunchedEffect(Unit) {
                val dao = EventDatabase.getInstance(context).eventDao()
                val stored = withContext(Dispatchers.IO) { dao.getEvents() }
                dates = stored.mapNotNull { event ->
                    event.toModel().date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                }.toSet()
            }
            CalendarView(eventDates = dates)
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
