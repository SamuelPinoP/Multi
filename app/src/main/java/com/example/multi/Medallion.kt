package com.example.multi

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Star
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Enum describing each clickable segment of the medallion. */
enum class MedallionSegment { WEEKLY_GOALS, CALENDAR, EVENTS, WORKOUT, NOTES }

/**
 * Basic button used by [Medallion] segments.
 */
@Composable
private fun SegmentButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    square: Boolean = true
) {
    val cardModifier = if (square) {
        modifier.aspectRatio(1f)
    } else {
        modifier
    }
    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(containerColor = color),
        modifier = cardModifier.clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Displays four clickable squares representing calendar, events, workout and
 * notes.
 */
@Composable
fun Medallion(
    modifier: Modifier = Modifier,
    onSegmentClick: (MedallionSegment) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFFE57373))) { append("M") }
                withStyle(style = SpanStyle(color = Color(0xFFF06292))) { append("u") }
                withStyle(style = SpanStyle(color = Color(0xFFBA68C8))) { append("l") }
                withStyle(style = SpanStyle(color = Color(0xFF4FC3F7))) { append("t") }
                withStyle(style = SpanStyle(color = Color(0xFF81C784))) { append("i") }
            },
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        SegmentButton(
            label = "Weekly Goals",
            icon = Icons.Filled.Star,
            color = Color(0xFFE1BEE7),
            onClick = { onSegmentClick(MedallionSegment.WEEKLY_GOALS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            square = false
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentButton(
                label = stringResource(R.string.label_calendar),
                icon = Icons.Filled.CalendarMonth,
                color = Color(0xFFBBDEFB),
                onClick = { onSegmentClick(MedallionSegment.CALENDAR) },
                modifier = Modifier.weight(1f)
            )
            SegmentButton(
                label = stringResource(R.string.label_events),
                icon = Icons.Filled.Event,
                color = Color(0xFFC8E6C9),
                onClick = { onSegmentClick(MedallionSegment.EVENTS) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentButton(
                label = stringResource(R.string.label_workout),
                icon = Icons.Filled.FitnessCenter,
                color = Color(0xFFFFF9C4),
                onClick = { onSegmentClick(MedallionSegment.WORKOUT) },
                modifier = Modifier.weight(1f)
            )
            SegmentButton(
                label = stringResource(R.string.label_notes),
                icon = Icons.Filled.Note,
                color = Color(0xFFFFCCBC),
                onClick = { onSegmentClick(MedallionSegment.NOTES) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Simple screen displaying the [Medallion] in the center. */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MedallionScreen() {
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
                        val text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate()
                            date.toString()
                        } else {
                            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            fmt.format(Date(millis))
                        }
                        Toast.makeText(context, "Selected: $text", Toast.LENGTH_SHORT).show()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Medallion { segment ->
            if (segment == MedallionSegment.CALENDAR) {
                showPicker = true
            } else {
                val cls = when (segment) {
                    MedallionSegment.WEEKLY_GOALS -> WeeklyGoalsActivity::class.java
                    MedallionSegment.EVENTS -> EventsActivity::class.java
                    MedallionSegment.WORKOUT -> WorkoutActivity::class.java
                    MedallionSegment.NOTES -> NotesActivity::class.java
                    else -> CalendarActivity::class.java
                }
                context.startActivity(Intent(context, cls))
            }
        }
    }
}
