package com.example.multi

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.time.temporal.WeekFields
import java.util.Locale

class RecordActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                title = { Text("Record", color = Color.Black) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val formatter = DateTimeFormatter.ofPattern("M/d/yyyy")
            val grouped = records.groupBy { it.startDate }
            grouped.forEach { (start, list) ->
                val startDate = LocalDate.parse(start)
                val endDate = LocalDate.parse(list.first().endDate)
                val month = startDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                val weekOfMonth = startDate.get(WeekFields.ISO.weekOfMonth())
                item {
                    Text(
                        "$month - Week $weekOfMonth - ${startDate.format(formatter)} - ${endDate.format(formatter)}",
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    )
                }
                items(list) { rec ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val complete = rec.completed >= rec.frequency
                        Text(
                            if (complete) "Completed" else "Uncomplete",
                            color = if (complete) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 12.sp
                        )
                        Text(
                            "${rec.header} ${rec.completed}/${rec.frequency}",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
