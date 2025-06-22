package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.Event

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val context = LocalContext.current
            val events = remember { mutableStateListOf<Event>() }

            LaunchedEffect(Unit) {
                val dao = EventDatabase.getInstance(context).eventDao()
                val stored = withContext(Dispatchers.IO) { dao.getEvents() }
                events.clear()
                events.addAll(stored.map { it.toModel() })
            }

            CalendarView(events = events)
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
