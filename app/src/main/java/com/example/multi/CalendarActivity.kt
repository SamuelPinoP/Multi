package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.material.Text
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val context = LocalContext.current
            val highlights by produceState(initialValue = emptySet<LocalDate>(), context) {
                val dao = EventDatabase.getInstance(context).eventDao()
                value = withContext(Dispatchers.IO) {
                    dao.getEvents().mapNotNull { event ->
                        event.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    }.toSet()
                }
            }
            CalendarView(highlighted = highlights)
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
