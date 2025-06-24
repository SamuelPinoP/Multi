package com.example.multi

import android.os.Build
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

/** Activity showing a calendar with event days highlighted. */
class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            EventsCalendarScreen()
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
