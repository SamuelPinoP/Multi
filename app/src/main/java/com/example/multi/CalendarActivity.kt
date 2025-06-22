package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.material.Text
import android.os.Build

/** Activity hosting the [MaterialCalendarScreen]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MaterialCalendarScreen()
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
