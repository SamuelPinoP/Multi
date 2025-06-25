package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.Calendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** Simple calendar display using the Kizitonwose Compose library. */
@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun CalendarView(date: LocalDate = LocalDate.now()) {
    val startMonth = YearMonth.from(date).minusMonths(12)
    val endMonth = YearMonth.from(date).plusMonths(12)
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = YearMonth.from(date),
        firstDayOfWeek = DayOfWeek.SUNDAY
    )

    Calendar(
        modifier = Modifier.padding(16.dp),
        state = state,
        dayContent = { dayState ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayState.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}
