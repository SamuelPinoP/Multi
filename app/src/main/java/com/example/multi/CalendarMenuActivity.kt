package com.example.multi

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarMenuActivity : SegmentActivity("") {
    @Composable
    override fun SegmentContent() {
        CalendarMenuScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarMenuScreen() {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState()

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showPicker = false
                    pickerState.selectedDateMillis?.let { millis ->
                        val dateStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()
                        } else {
                            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            fmt.format(Date(millis))
                        }
                        val intent = Intent(context, EventsActivity::class.java)
                        intent.putExtra(EXTRA_DATE, dateStr)
                        context.startActivity(intent)
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MenuCardButton(
            label = "Events in Calendar",
            icon = Icons.Default.Event,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = { /* No action for now */ },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        MenuCardButton(
            label = "Weekly Goals View",
            icon = Icons.Default.Flag,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            onClick = { /* No action for now */ },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        MenuCardButton(
            label = "Calendar Display",
            icon = Icons.Default.CalendarMonth,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            onClick = { showPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun MenuCardButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

