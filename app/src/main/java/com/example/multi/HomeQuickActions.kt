package com.example.multi

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A single-row bar of 4 luxe-looking quick-action buttons:
 * Notes • Goals • Events • Calendar (or Dates)
 *
 * Place it under the medallion on the Home screen.
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    calendarLabel: String = "Calendar",
    cornerRadius: Dp = 22.dp,
    height: Dp = 112.dp,          // BIGGER buttons
    gap: Dp = 12.dp,              // space only BETWEEN buttons
    borderWidth: Dp = 1.dp        // MUCH thinner border
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            // Bump this padding above 0.dp if you ever want space on the left/right edges.
            .padding(horizontal = 0.dp),          // ensures edge-to-edge
        horizontalArrangement = Arrangement.spacedBy(gap), // even gaps only between
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = "Notes",
            shape = shape,
            borderWidth = borderWidth,
            height = height
        ) { context.startActivity(Intent(context, NotesActivity::class.java)) }

        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = "Goals",
            shape = shape,
            borderWidth = borderWidth,
            height = height
        ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = "Events",
            shape = shape,
            borderWidth = borderWidth,
            height = height
        ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = calendarLabel,
            shape = shape,
            borderWidth = borderWidth,
            height = height
        ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
    }
}

/** Modern, minimal: transparent fill, thin black outline, black label (titleMedium). */
@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    shape: RoundedCornerShape,
    borderWidth: Dp,
    height: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .border(BorderStroke(borderWidth, Color.Black), shape)
            .background(Color.Transparent)
            .clickable(onClick = onClick), // use default ripple for a modern feel
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium, // original size
            maxLines = 1
        )
    }
}