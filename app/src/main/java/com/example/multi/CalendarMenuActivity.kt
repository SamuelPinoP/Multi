package com.example.multi

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.multi.ui.theme.MultiTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CalendarMenuScreen()
                }
            }
        }
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
            gradient = Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer
                )
            ),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(24.dp),
            onClick = {
                context.startActivity(
                    Intent(context, KizCalendarActivity::class.java)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        MenuCardButton(
            label = "Weekly Goals View",
            icon = Icons.Default.Flag,
            gradient = Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.tertiaryContainer
                )
            ),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = CutCornerShape(16.dp),
            onClick = {
                context.startActivity(
                    Intent(context, WeeklyGoalsActivity::class.java)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        MenuCardButton(
            label = "Calendar Display",
            icon = Icons.Default.CalendarMonth,
            gradient = Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.primaryContainer
                )
            ),
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            shape = RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp),
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
    gradient: Brush,
    contentColor: Color,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = contentColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text(label, style = MaterialTheme.typography.titleMedium, color = contentColor)
            }
        }
    }
}

