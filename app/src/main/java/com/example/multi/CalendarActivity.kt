package com.example.multi

import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    override fun onSwipeLeft() {
        startActivity(Intent(this, EventsActivity::class.java))
    }

    override fun onSwipeRight() {
        startActivity(Intent(this, WeeklyGoalsActivity::class.java))
    }

    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CalendarView()
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
