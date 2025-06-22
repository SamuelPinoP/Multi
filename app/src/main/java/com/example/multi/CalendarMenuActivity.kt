package com.example.multi

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme

class CalendarMenuActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        CalendarMenuScreen()
    }
}

@Composable
private fun MenuButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        elevation = CardDefaults.elevatedCardElevation(),
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.material3.Icon(icon, contentDescription = null)
                Spacer(modifier = Modifier.height(8.dp))
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarMenuScreen() {
    val context = LocalContext.current
    var showCalendar by remember { mutableStateOf(false) }

    if (showCalendar) {
        AlertDialog(
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(onClick = { showCalendar = false }) { Text("Close") }
            },
            text = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CalendarView()
                } else {
                    Text("Calendar requires Android O or higher")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MenuButton(
            label = "Events in Calendar",
            icon = Icons.Default.Event,
            onClick = { /* No action for now */ },
            modifier = Modifier.weight(1f)
        )

        MenuButton(
            label = "Weekly Goals View",
            icon = Icons.Default.Flag,
            onClick = { /* No action for now */ },
            modifier = Modifier.weight(1f)
        )

        MenuButton(
            label = "Calendar Display",
            icon = Icons.Default.DateRange,
            onClick = { showCalendar = true },
            modifier = Modifier.weight(1f)
        )
    }
}

