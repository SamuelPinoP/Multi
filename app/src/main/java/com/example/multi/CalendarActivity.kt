package com.example.multi

import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.material.Text
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val context = LocalContext.current
            var dates by remember { mutableStateOf(setOf<java.time.LocalDate>()) }

            LaunchedEffect(Unit) {
                val dao = EventDatabase.getInstance(context).eventDao()
                val stored = withContext(Dispatchers.IO) { dao.getEvents() }
                dates = stored.mapNotNull { event ->
                    event.date?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull() }
                }.toSet()
            }

            CalendarView(highlightedDates = dates)
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
