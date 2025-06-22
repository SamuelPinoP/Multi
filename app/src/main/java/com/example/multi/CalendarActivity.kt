package com.example.multi

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val context = LocalContext.current
            var events by remember { mutableStateOf<Map<LocalDate, List<Event>>>(emptyMap()) }

            LaunchedEffect(Unit) {
                val dao = EventDatabase.getInstance(context).eventDao()
                val stored = withContext(Dispatchers.IO) { dao.getEvents() }
                events = stored.mapNotNull { entity ->
                    entity.date?.let { LocalDate.parse(it) to entity.toModel() }
                }.groupBy({ it.first }, { it.second })
            }

            CalendarView(events = events) { date ->
                val intent = Intent(context, EventsActivity::class.java)
                intent.putExtra(EXTRA_DATE, date.toString())
                context.startActivity(intent)
            }
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
