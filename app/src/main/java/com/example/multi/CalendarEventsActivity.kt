package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text

/**
 * Simple placeholder activity launched from the "Events in Calendar" button.
 */
class CalendarEventsActivity : SegmentActivity("Calendar Events") {
    @Composable
    override fun SegmentContent() {
        Text("events")
    }
}
