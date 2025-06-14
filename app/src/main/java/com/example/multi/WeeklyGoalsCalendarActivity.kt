package com.example.multi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.WeekViewDisplayable
import com.example.multi.ui.theme.MultiTheme
import java.util.Calendar

class WeeklyGoalsCalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                WeekViewScreen()
            }
        }
    }
}

data class GoalEvent(
    val id: Long,
    val title: String,
    val start: Calendar,
    val end: Calendar
) : WeekViewDisplayable<GoalEvent> {
    override fun toWeekViewEntity(): WeekViewEntity {
        return WeekViewEntity.Event.Builder(this)
            .setId(id)
            .setTitle(title)
            .setStartTime(start)
            .setEndTime(end)
            .build()
    }
}

@Composable
private fun WeekViewScreen() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WeekView(context).also { view ->
                val now = Calendar.getInstance()
                val start = now.clone() as Calendar
                start.set(Calendar.HOUR_OF_DAY, 9)
                val end = start.clone() as Calendar
                end.add(Calendar.HOUR_OF_DAY, 1)
                view.submit(listOf(GoalEvent(1, "Sample Goal", start, end)))
            }
        }
    )
}
