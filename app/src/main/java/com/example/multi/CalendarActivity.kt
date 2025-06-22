package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.Text
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val context = LocalContext.current
            var dates by remember { mutableStateOf(setOf<LocalDate>()) }
            LaunchedEffect(Unit) {
                val dao = EventDatabase.getInstance(context).eventDao()
                val events = withContext(Dispatchers.IO) { dao.getEvents() }
                dates = events.mapNotNull { it.toModel().date?.let { d ->
                    runCatching { LocalDate.parse(d) }.getOrNull() } }.toSet()
            }
            CalendarView(highlightedDates = dates)
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
