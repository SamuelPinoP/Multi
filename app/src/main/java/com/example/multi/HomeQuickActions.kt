package com.example.multi

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A single-row bar of 4 bold outlined quick-action buttons:
 * Notes • Goals • Events • Calendar (or Dates)
 *
 * Place it under the medallion on the Home screen.
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    // Set to "Dates" if you prefer that label:
    calendarLabel: String = "Calendar",
    cornerRadius: Dp = 24.dp,
    height: Dp = 112.dp
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            modifier = Modifier.weight(1f), // weight comes from RowScope here
            label = "Notes",
            shape = shape,
            height = height,
        ) { context.startActivity(Intent(context, NotesActivity::class.java)) }

        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = "Goals",
            shape = shape,
            height = height,
        ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = "Events",
            shape = shape,
            height = height,
        ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = calendarLabel,
            shape = shape,
            height = height,
        ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
    }
}

/**
 * Minimal quick action button with a bold outline and transparent center so the
 * label stands alone.
 */
@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,              // <-- accept modifier so Row can pass weight
    label: String,
    shape: RoundedCornerShape,
    height: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .shadow(elevation = 10.dp, shape = shape)
            .clip(shape)
            .background(Color.Transparent)
            .border(BorderStroke(2.dp, Color.Black), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
