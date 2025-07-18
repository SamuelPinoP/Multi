package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CalendarView()
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
