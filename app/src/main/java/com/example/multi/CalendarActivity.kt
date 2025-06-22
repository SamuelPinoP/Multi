package com.example.multi

import androidx.compose.runtime.Composable

/** Activity hosting the [MaterialCalendar] view. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        MaterialCalendar()
    }
}
