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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/** Enum describing each clickable segment of the medallion. */
enum class MedallionSegment { WEEKLY_GOALS, CALENDAR, EVENTS, WORKOUT, NOTES }

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
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            color = Color(0xFFE1BEE7),
            onClick = { onSegmentClick(MedallionSegment.WEEKLY_GOALS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            square = false
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentButton(
                label = stringResource(R.string.label_calendar),
                color = Color(0xFFBBDEFB),
                onClick = { onSegmentClick(MedallionSegment.CALENDAR) },
                modifier = Modifier.weight(1f)
            )
            SegmentButton(
                label = stringResource(R.string.label_events),
                color = Color(0xFFC8E6C9),
                onClick = { onSegmentClick(MedallionSegment.EVENTS) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SegmentButton(
                label = stringResource(R.string.label_workout),
                color = Color(0xFFFFF9C4),
                onClick = { onSegmentClick(MedallionSegment.WORKOUT) },
                modifier = Modifier.weight(1f)
            )
            SegmentButton(
                label = stringResource(R.string.label_notes),
                color = Color(0xFFFFCCBC),
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
        contentAlignment = Alignment.TopCenter
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
