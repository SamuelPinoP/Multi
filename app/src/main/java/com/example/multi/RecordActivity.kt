package com.example.multi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.data.toEntity
import com.example.multi.ui.theme.MultiTheme
import com.example.multi.DayButtonsRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
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
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var selectedRecordIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

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
            val grouped = records.withIndex().groupBy { it.value.weekStart to it.value.weekEnd }
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
                    items(list) { pair ->
                        val rec = pair.value
                        val index = pair.index
                        val done = rec.completed >= rec.frequency
                        val isEditing = editingIndex == index
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEditing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
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
                                        IconButton(onClick = {
                                            editingIndex = if (isEditing) null else index
                                        }) {
                                            Icon(
                                                imageVector = if (isEditing) Icons.Filled.Done else Icons.Filled.Edit,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                DayButtonsRow(states = rec.dayStates) { dayIdx ->
                                    if (isEditing) {
                                        selectedRecordIndex = index
                                        selectedDayIndex = dayIdx
                                    }
                                }
                            }
                        }
                    }
                }

                val rIndex = selectedRecordIndex
                val dIndex = selectedDayIndex
                if (rIndex != null && dIndex != null) {
                    DayChoiceDialog(
                        onDismiss = { selectedRecordIndex = null; selectedDayIndex = null },
                        onMiss = {
                            val rec = records[rIndex]
                            val chars = rec.dayStates.toCharArray()
                            chars[dIndex] = 'M'
                            val completed = chars.count { it == 'C' }
                            val updated = rec.copy(dayStates = String(chars), completed = completed)
                            records[rIndex] = updated
                            scope.launch {
                                val dao = EventDatabase.getInstance(context).weeklyGoalRecordDao()
                                withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            }
                        },
                        onComplete = {
                            val rec = records[rIndex]
                            val chars = rec.dayStates.toCharArray()
                            chars[dIndex] = 'C'
                            val completed = chars.count { it == 'C' }
                            val updated = rec.copy(dayStates = String(chars), completed = completed)
                            records[rIndex] = updated
                            scope.launch {
                                val dao = EventDatabase.getInstance(context).weeklyGoalRecordDao()
                                withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            }
                        }
                    )
                }
            }
        }
    }
}
