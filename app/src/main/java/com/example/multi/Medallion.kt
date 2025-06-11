package com.example.multi

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign

/** Enum describing each clickable segment of the medallion. */
enum class MedallionSegment { WEEKLY_GOALS, STONE, IRON, WOOD, MAGMA }

@Composable
private fun SegmentButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    square: Boolean = true
) {
    val boxModifier = if (square) {
        modifier.aspectRatio(1f)
    } else {
        modifier
    }
    Box(
        modifier = boxModifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
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
            .size(350.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SegmentButton(
            label = "Weekly Goals",
            color = Color(0xFFE1BEE7),
            onClick = { onSegmentClick(MedallionSegment.WEEKLY_GOALS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            square = false
        )
        Spacer(modifier = Modifier.height(80.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentButton(
                label = stringResource(R.string.label_calendar),
                color = Color(0xFFBBDEFB),
                onClick = { onSegmentClick(MedallionSegment.STONE) },
                modifier = Modifier.weight(1f)
            )
            SegmentButton(
                label = stringResource(R.string.label_events),
                color = Color(0xFFC8E6C9),
                onClick = { onSegmentClick(MedallionSegment.IRON) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(270.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentButton(
                label = stringResource(R.string.label_workout),
                color = Color(0xFFFFF9C4),
                onClick = { onSegmentClick(MedallionSegment.WOOD) },
                modifier = Modifier.weight(1f)
            )
            SegmentButton(
                label = stringResource(R.string.label_notes),
                color = Color(0xFFFFCCBC),
                onClick = { onSegmentClick(MedallionSegment.MAGMA) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Simple screen displaying the [Medallion] in the center. */
@Composable
fun MedallionScreen() {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Medallion { segment ->
            val cls = when (segment) {
                MedallionSegment.WEEKLY_GOALS -> WeeklyGoalsActivity::class.java
                MedallionSegment.STONE -> CalendarActivity::class.java
                MedallionSegment.IRON -> EventsActivity::class.java
                MedallionSegment.WOOD -> WorkoutActivity::class.java
                MedallionSegment.MAGMA -> NotesActivity::class.java
            }
            context.startActivity(Intent(context, cls))
        }
    }
}
