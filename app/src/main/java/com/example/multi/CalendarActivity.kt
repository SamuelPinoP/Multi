package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.material.Text
import androidx.compose.ui.res.stringResource

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity(R.string.label_calendar) {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CalendarView()
        } else {
            Text(stringResource(R.string.calendar_requires_o))
        }
    }
}
