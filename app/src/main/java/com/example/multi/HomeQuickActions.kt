package com.example.multi

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
    calendarLabel: String = "Calendar",
    cornerRadius: Dp = 22.dp,
    height: Dp = 112.dp,          // BIGGER buttons
    gap: Dp = 2.dp,              // space only BETWEEN buttons
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)
    val scheme = MaterialTheme.colorScheme

    val quickActions = listOf(
        QuickActionDescriptor(
            label = "Notes",
            icon = Icons.Filled.Edit,
            gradient = Brush.linearGradient(
                listOf(scheme.primary, scheme.tertiary)
            ),
            contentColor = scheme.onPrimary,
            onClick = { context.startActivity(Intent(context, NotesActivity::class.java)) }
        ),
        QuickActionDescriptor(
            label = "Goals",
            icon = Icons.Filled.Flag,
            gradient = Brush.linearGradient(
                listOf(scheme.secondary, scheme.primaryContainer)
            ),
            contentColor = scheme.onSecondary,
            onClick = { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }
        ),
        QuickActionDescriptor(
            label = "Events",
            icon = Icons.Filled.Event,
            gradient = Brush.linearGradient(
                listOf(scheme.tertiary, scheme.secondaryContainer)
            ),
            contentColor = scheme.onTertiary,
            onClick = { context.startActivity(Intent(context, EventsActivity::class.java)) }
        ),
        QuickActionDescriptor(
            label = calendarLabel,
            icon = Icons.Filled.CalendarMonth,
            gradient = Brush.linearGradient(
                listOf(scheme.primaryContainer, scheme.tertiaryContainer)
            ),
            contentColor = scheme.onPrimaryContainer,
            onClick = { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .padding(horizontal = 0.dp),          // ensures edge-to-edge
        horizontalArrangement = Arrangement.spacedBy(gap), // even gaps only between
        verticalAlignment = Alignment.CenterVertically
    ) {
        quickActions.forEach { action ->
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = action.label,
                icon = action.icon,
                gradient = action.gradient,
                contentColor = action.contentColor,
                shape = shape,
                height = height,
                onClick = action.onClick
            )
        }
    }
}

private data class QuickActionDescriptor(
    val label: String,
    val icon: ImageVector,
    val gradient: Brush,
    val contentColor: Color,
    val onClick: () -> Unit
)

/** Luxe glassmorphism inspired buttons with gradient fills and floating shadows. */
@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    gradient: Brush,
    contentColor: Color,
    shape: RoundedCornerShape,
    height: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .shadow(elevation = 18.dp, shape = shape, clip = false)
            .clip(shape)
            .background(gradient)
            .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                    )
                )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(height * 0.45f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(height * 0.3f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
