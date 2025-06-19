package com.example.multi

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.material.Text
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.multi.Event
import com.example.multi.EventDialog

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

            CalendarView(onDateSelected = { date -> selectedDate = date })

            selectedDate?.let { date ->
                EventDialog(
                    initial = Event(0L, "", "", date.toString()),
                    onDismiss = { selectedDate = null },
                    onSave = { title, desc, dateStr ->
                        selectedDate = null
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).eventDao()
                            withContext(Dispatchers.IO) {
                                dao.insert(Event(title = title, description = desc, date = dateStr).toEntity())
                            }
                            Toast.makeText(context, "Event saved", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
