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
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.ui.theme.MultiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

class RecordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    RecordScreen { finish() }
                } else {
                    Text("Record requires Android O or higher")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val records = remember { mutableStateListOf<WeeklyRecord>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).weeklyRecordDao()
        val stored = withContext(Dispatchers.IO) { dao.getRecords() }
        records.clear()
        records.addAll(stored.map { it.toModel() })
    }

    ScaffoldWithBar(onBack) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            val grouped = records.groupBy { it.weekStart }
            grouped.forEach { (start, list) ->
                item {
                    val startDate = LocalDate.parse(start)
                    val endDate = LocalDate.parse(list.first().weekEnd)
                    val month = startDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                    val weekOfMonth = startDate.get(WeekFields.of(Locale.getDefault()).weekOfMonth())
                    Text(
                        text = "$month - Week $weekOfMonth - $start - ${list.first().weekEnd}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(list) { rec ->
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
                        val completed = rec.completed >= rec.frequency
                        Text(
                            text = if (completed) "Completed" else "Incomplete",
                            color = if (completed) Color(0xFF4CAF50) else Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${rec.header} ${rec.completed}/${rec.frequency}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun ScaffoldWithBar(onBack: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material.Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                title = { Text("Record", color = Color.Black, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
            )
        }
    ) { inner ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(inner), contentAlignment = Alignment.TopCenter) {
            content()
        }
    }
}
