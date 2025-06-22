package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.Text
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
            val highlights = remember { mutableStateListOf<LocalDate>() }
            LaunchedEffect(Unit) {
                val dao = EventDatabase.getInstance(context).eventDao()
                val stored = withContext(Dispatchers.IO) { dao.getEvents() }
                highlights.clear()
                stored.forEach { entity ->
                    entity.date?.let { dateStr ->
                        try {
                            highlights.add(LocalDate.parse(dateStr))
                        } catch (_: Exception) {}
                    }
                }
            }
            CalendarView(highlights = highlights.toSet())
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
