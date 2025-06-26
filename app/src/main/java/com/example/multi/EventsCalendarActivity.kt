package com.example.multi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.multi.ui.theme.MultiTheme

import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.VerticalCalendar
import java.time.YearMonth

class EventsCalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    KizitonwoseCalendarScreen()
                }
            }
        }
    }
}

@Composable
fun KizitonwoseCalendarScreen() {
    val currentMonth = YearMonth.now()
    val state = rememberCalendarState(
        startMonth = currentMonth.minusMonths(12),
        endMonth = currentMonth.plusMonths(12),
        firstVisibleMonth = currentMonth
    )
    VerticalCalendar(state = state, dayContent = { day ->
        androidx.compose.material3.Text(day.date.dayOfMonth.toString(),
            modifier = Modifier.padding(8.dp))
    })
}
