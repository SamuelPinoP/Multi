package com.example.multi

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign


/** Enum describing each clickable segment of the medallion. */
enum class MedallionSegment { WEEKLY_GOALS, CALENDAR, EVENTS, WORKOUT, NOTES }

/**
 * Basic button used by [Medallion] segments.
 */
@Composable
private fun SegmentButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    square: Boolean = true
) {
    val cardModifier = if (square) {
        modifier.aspectRatio(1f)
    } else {
        modifier
    }
    val contentColor = contentColorFor(containerColor)
    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp),
        modifier = cardModifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
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
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Multi",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        SegmentButton(
            label = stringResource(R.string.label_weekly_goals),
            icon = Icons.Default.Flag,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = { onSegmentClick(MedallionSegment.WEEKLY_GOALS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            square = false
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentButton(
                label = stringResource(R.string.label_calendar),
                icon = Icons.Default.DateRange,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { onSegmentClick(MedallionSegment.CALENDAR) },
                modifier = Modifier.weight(1f)
            )
            SegmentButton(
                label = stringResource(R.string.label_events),
                icon = Icons.Default.Event,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = { onSegmentClick(MedallionSegment.EVENTS) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentButton(
                label = stringResource(R.string.label_workout),
                icon = Icons.Default.FitnessCenter,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onSegmentClick(MedallionSegment.WORKOUT) },
                modifier = Modifier.weight(1f)
            )
            SegmentButton(
                label = stringResource(R.string.label_notes),
                icon = Icons.Default.Note,
                containerColor = MaterialTheme.colorScheme.inversePrimary,
                onClick = { onSegmentClick(MedallionSegment.NOTES) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Simple screen displaying the [Medallion] in the center. */
@Composable
fun MedallionScreen() {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Medallion { segment ->
            val cls = when (segment) {
                MedallionSegment.WEEKLY_GOALS -> WeeklyGoalsActivity::class.java
                MedallionSegment.CALENDAR -> CalendarActivity::class.java
                MedallionSegment.EVENTS -> EventsActivity::class.java
                MedallionSegment.WORKOUT -> WorkoutActivity::class.java
                MedallionSegment.NOTES -> NotesActivity::class.java
            }
            context.startActivity(Intent(context, cls))
        }
    }
}
