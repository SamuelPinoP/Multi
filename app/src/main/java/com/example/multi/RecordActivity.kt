package com.example.multi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.ui.theme.MultiTheme
import kotlinx.coroutines.Dispatchers
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
                RecordScreen { finish() }
            }
        }
    }
}

@Composable
fun RecordScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val records = remember { mutableStateListOf<WeeklyGoalRecord>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).weeklyGoalRecordDao()
        val stored = withContext(Dispatchers.IO) { dao.getRecords() }
        records.clear()
        records.addAll(stored.map { it.toModel() })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(135.dp),
                backgroundColor = Color.White,
                elevation = 4.dp,
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Record",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }
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
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                        )
                    }
                    items(list) { rec ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            val done = rec.completed >= rec.frequency
                            Text(
                                text = if (done) "Completed" else "Uncomplete",
                                color = if (done) Color.Green else Color.Red,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${rec.header} ${rec.completed}/${rec.frequency}",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }
    }
}
