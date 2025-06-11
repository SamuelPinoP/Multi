package com.example.multi

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.Text
import androidx.compose.material.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.multi.ui.theme.MultiTheme

open class SegmentActivity(private val segmentTitle: String) : ComponentActivity() {
    /** Content displayed inside the [SegmentScreen]. */
    @Composable
    open fun SegmentContent() {
        Text(segmentTitle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                SegmentScreen(
                    title = segmentTitle,
                    onBack = { finish() },
                    onClose = { finishAffinity() }
                ) {
                    SegmentContent()
                }
            }
        }
    }
}

class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        CalendarView()
    }
}
class EventsActivity : SegmentActivity("Events") {
    @Composable
    override fun SegmentContent() {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                super.SegmentContent()
            }
            FloatingActionButton(
                onClick = {
                    startActivity(Intent(this@EventsActivity, CreateEventActivity::class.java))
                },
                backgroundColor = Color(0xFF4CAF50),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Event",
                    tint = Color.White
                )
            }
        }
    }
}
class WorkoutActivity : SegmentActivity("Workout") {
    @Composable
    override fun SegmentContent() {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Today's workout", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("\u2022 Push-ups\n\u2022 Sit-ups\n\u2022 Squats")
        }
    }
}

class NotesActivity : SegmentActivity("Notes") {
    @Composable
    override fun SegmentContent() {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Notes", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("This is where your notes will appear.")
        }
    }
}

class WeeklyGoalsActivity : SegmentActivity("Weekly Goals") {
    @Composable
    override fun SegmentContent() {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This week's goals", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("\u2022 Goal 1\n\u2022 Goal 2\n\u2022 Goal 3")
        }
    }
}

@Composable
fun SegmentScreen(
    title: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(135.dp),
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
