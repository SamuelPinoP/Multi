package com.example.multi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.multi.ui.theme.MultiTheme

open class SegmentActivity(private val segmentTitle: String) : ComponentActivity() {
    open val screenContent: @Composable BoxScope.() -> Unit = {
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
                    onClose = { finishAffinity() },
                    content = screenContent
                )
            }
        }
    }
}

class CalendarActivity : SegmentActivity("Calendar")
class EventsActivity : SegmentActivity("Events") {
    override val screenContent: @Composable BoxScope.() -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { /* TODO: open create event screen */ },
                backgroundColor = Color(0xFF4CAF50),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(56.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Event",
                    tint = Color.White
                )
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Events")
            }
        }
    }
}
class WorkoutActivity : SegmentActivity("Workout")
class NotesActivity : SegmentActivity("Notes")
class WeeklyGoalsActivity : SegmentActivity("Weekly Goals")

@Composable
fun SegmentScreen(
    title: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(80.dp),
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
