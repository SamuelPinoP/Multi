package com.example.multi

import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import kotlin.collections.buildList
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.Text
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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
        EventsScreen()
    }
}

data class Event(var title: String, var description: String)

@Composable
private fun EventsScreen() {
    val listSaver = listSaver<MutableList<Event>, String>(
        save = { list ->
            buildList<String> {
                list.forEach { event ->
                    add(event.title)
                    add(event.description)
                }
            }
        },
        restore = { items ->
            items.chunked(2).map { Event(it[0], it[1]) }.toMutableStateList()
        }
    )
    val events = rememberSaveable(saver = listSaver) { mutableStateListOf<Event>() }
    var editingIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(events) { index, event ->
                Card(
                    elevation = CardDefaults.cardElevation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingIndex = index }
                ) {
                    Text(
                        text = "${index + 1} - ${event.title}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { editingIndex = -1 },
            backgroundColor = Color(0xFF4CAF50),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 68.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
        }

        val index = editingIndex
        if (index != null) {
            val isNew = index < 0
            val event = if (isNew) Event("", "") else events[index]
            EventDialog(
                initial = event,
                onDismiss = { editingIndex = null },
                onSave = { title, desc ->
                    if (isNew) {
                        events.add(Event(title, desc))
                    } else {
                        events[index] = Event(title, desc)
                    }
                    editingIndex = null
                },
                onDelete = if (isNew) null else {
                    {
                        events.removeAt(index)
                        editingIndex = null
                    }
                }
            )
        }
    }
}

@Composable
private fun EventDialog(
    initial: Event,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initial.title) }
    var description by remember { mutableStateOf(initial.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(title, description) }) { Text("Save") }
        },
        dismissButton = {
            Row {
                onDelete?.let { del ->
                    TextButton(onClick = del) { Text("Delete") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp), // optional: add space from top
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { /* TODO: Historial action */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp) // taller button
                            .padding(end = 8.dp) // space between
                    ) {
                        Text("Historial", color = Color.White, fontSize = 20.sp)
                    }
                    Button(
                        onClick = { /* TODO: Edit action */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA68C8)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .padding(start = 8.dp)
                    ) {
                        Text("Edit", color = Color.White, fontSize = 20.sp)
                    }
                }
            }
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
                title = {
                    Text(
                        text = title,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
