package com.example.multi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.data.toEntity
import com.example.multi.ui.theme.MultiTheme
import com.example.multi.DayButtonsRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class RecordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                RecordScreen()
            }
        }
    }
}

@Composable
private fun DayChoiceDialog(
    onDismiss: () -> Unit,
    onMiss: () -> Unit,
    onComplete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onComplete()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Icon(Icons.Default.Check, contentDescription = null) }
        },
        dismissButton = {
            Button(
                onClick = {
                    onMiss()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Icon(Icons.Default.Close, contentDescription = null) }
        },
        title = { Text("Mark Day") },
        text = { Text("Choose status") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen() {
    val context = LocalContext.current
    val records = remember { mutableStateListOf<WeeklyGoalRecord>() }
    val scope = rememberCoroutineScope()
    var editingId by remember { mutableStateOf<Long?>(null) }
    var selectedRecordId by remember { mutableStateOf<Long?>(null) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).weeklyGoalRecordDao()
        val stored = withContext(Dispatchers.IO) { dao.getRecords() }
        records.clear()
        records.addAll(stored.map { it.toModel() })
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .height(80.dp)
                    .shadow(4.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                title = {
                    Text(
                        text = "Record",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp)
                    )
                },
                navigationIcon = {}
            )
        }
    ) { padding ->
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No records yet")
            }
        } else {
            val grouped = records.groupBy { it.weekStart to it.weekEnd }
            val dateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy")

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                grouped.forEach { (range, list) ->
                    item {
                        val start = LocalDate.parse(range.first)
                        val end = LocalDate.parse(range.second)
                        val month = start.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        val weekOfMonth = (start.dayOfMonth - 1) / 7 + 1
                        Text(
                            text = "$month - Week $weekOfMonth - ${start.format(dateFormatter)} - ${end.format(dateFormatter)}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    itemsIndexed(list) { _, rec ->
                        val done = rec.completed >= rec.frequency
                        val editing = editingId == rec.id
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (editing) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(rec.header, style = MaterialTheme.typography.bodyLarge)
                                        Text("${rec.completed}/${rec.frequency}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (done) Icons.Filled.Check else Icons.Filled.Close,
                                            contentDescription = null,
                                            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                        if (editing) {
                                            IconButton(onClick = { editingId = null }) {
                                                Icon(Icons.Filled.Check, contentDescription = null)
                                            }
                                        } else {
                                            IconButton(onClick = { editingId = rec.id }) {
                                                Icon(Icons.Filled.Edit, contentDescription = null)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                DayButtonsRow(states = rec.dayStates) { dayIndex ->
                                    if (editing) {
                                        selectedRecordId = rec.id
                                        selectedDayIndex = dayIndex
                                    }
                                }
                            }
                        }
                    }
                }
            }
            val rId = selectedRecordId
            val dIndex = selectedDayIndex
            if (rId != null && dIndex != null) {
                DayChoiceDialog(
                    onDismiss = { selectedRecordId = null; selectedDayIndex = null },
                    onMiss = {
                        val idx = records.indexOfFirst { it.id == rId }
                        if (idx != -1) {
                            val rec = records[idx]
                            val chars = rec.dayStates.toCharArray()
                            chars[dIndex] = 'M'
                            val completed = chars.count { it == 'C' }
                            val updated = rec.copy(
                                dayStates = String(chars),
                                completed = completed
                            )
                            records[idx] = updated
                            scope.launch {
                                val dao = EventDatabase.getInstance(context).weeklyGoalRecordDao()
                                withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            }
                        }
                    },
                    onComplete = {
                        val idx = records.indexOfFirst { it.id == rId }
                        if (idx != -1) {
                            val rec = records[idx]
                            val chars = rec.dayStates.toCharArray()
                            chars[dIndex] = 'C'
                            val completed = chars.count { it == 'C' }
                            val updated = rec.copy(
                                dayStates = String(chars),
                                completed = completed
                            )
                            records[idx] = updated
                            scope.launch {
                                val dao = EventDatabase.getInstance(context).weeklyGoalRecordDao()
                                withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            }
                        }
                    }
                )
            }
        }
    }
}
