package com.example.multi

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

class RecordActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                RecordScreen(onBack = { finish() })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val records = remember { mutableStateListOf<WeeklyGoalRecord>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).weeklyGoalRecordDao()
        val stored = withContext(Dispatchers.IO) { dao.getRecords() }
        records.clear()
        records.addAll(stored.map { it.toModel() })
    }

    val grouped = records.groupBy { it.weekStart }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Green)
                    }
                },
                backgroundColor = Color.White
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            grouped.toSortedMap(compareByDescending<String> { it }).forEach { (start, list) ->
                item {
                    val startDate = LocalDate.parse(start)
                    val month = startDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    val weekOfMonth = startDate.get(WeekFields.of(Locale.getDefault()).weekOfMonth())
                    val formatter = DateTimeFormatter.ofPattern("M/d/yyyy")
                    val text = "$month - Week $weekOfMonth - ${startDate.format(formatter)} - ${startDate.plusDays(6).format(formatter)}"
                    Text(text, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(list) { rec ->
                    val complete = rec.completed >= rec.frequency
                    Text(
                        text = if (complete) "Completed" else "Uncomplete",
                        color = if (complete) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${rec.header} ${rec.completed}/${rec.frequency}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
