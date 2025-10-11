package com.example.multi

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // <-- needed for `val x by ...`
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    // Set to "Dates" if you prefer that label:
    calendarLabel: String = "Calendar",
    cornerRadius: Dp = 16.dp,
    height: Dp = 56.dp
) {
    val c = MaterialTheme.colorScheme
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .padding(horizontal = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            modifier = Modifier.weight(1f), // weight comes from RowScope here
            label = "Notes",
            icon = Icons.Filled.Note,
            start = c.primary.copy(alpha = 0.85f),
            end = c.tertiary.copy(alpha = 0.85f),
            shape = shape,
        ) { context.startActivity(Intent(context, NotesActivity::class.java)) }

        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = "Goals",
            icon = Icons.Filled.Flag,
            start = Color(0xFF39D98A),
            end = Color(0xFF12B886),
            shape = shape,
        ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = "Events",
            icon = Icons.Filled.Event,
            start = Color(0xFFFF8A5B),
            end = Color(0xFFFF6B3D),
            shape = shape,
        ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

        QuickActionButton(
            modifier = Modifier.weight(1f),
            label = calendarLabel,
            icon = Icons.Filled.DateRange,
            start = Color(0xFF8A80FF),
            end = Color(0xFF6C63FF),
            shape = shape,
        ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
    }
}

/** Fancy pill card with animated sheen + gradient border. */
@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,              // <-- accept modifier so Row can pass weight
    label: String,
    icon: ImageVector,
    start: Color,
    end: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    // Animated highlight sheen across the button
    val infinite = rememberInfiniteTransition(label = "qaSheen")
    val shift by infinite.animateFloat(
        initialValue = -300f, targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "qaShift"
    )

    val bg = Brush.linearGradient(listOf(start, end))
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            start.copy(alpha = 0.9f),
            end.copy(alpha = 0.9f),
            start.copy(alpha = 0.9f)
        ),
        tileMode = TileMode.Clamp
    )
    val sheen = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.35f),
            Color.Transparent
        )
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .shadow(elevation = 10.dp, shape = shape, ambientColor = end, spotColor = start)
            .clip(shape)
            .background(bg)
            .border(BorderStroke(1.dp, borderBrush), shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Sheen overlay (subtle animated sweep)
        Box(
            Modifier
                .matchParentSize()
                .padding(horizontal = 6.dp)
                .clip(shape)
                .background(sheen)
                .offset(x = shift.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1
            )
        }
    }
}
