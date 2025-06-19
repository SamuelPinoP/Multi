package com.example.multi

import androidx.compose.runtime.*
import androidx.compose.material.Text
import java.time.LocalDate


/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
            CalendarView(onDateSelected = { selectedDate = it })
            selectedDate?.let { date ->
                QuickEventDialog(date.toString()) { selectedDate = null }
            }
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
