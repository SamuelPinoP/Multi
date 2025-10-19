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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    cornerRadius: Dp = 24.dp,
    height: Dp = 112.dp,
    gap: Dp = 16.dp
) {
    val context = LocalContext.current
    val trackShape = RoundedCornerShape(cornerRadius)

    Surface(
        tonalElevation = 6.dp,
        shape = trackShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
                    )
                ),
                shape = trackShape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Notes",
                cornerRadius = cornerRadius,
                height = height
            ) { context.startActivity(Intent(context, NotesActivity::class.java)) }

            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Goals",
                cornerRadius = cornerRadius,
                height = height
            ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = "Events",
                cornerRadius = cornerRadius,
                height = height
            ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

            QuickActionButton(
                modifier = Modifier.weight(1f),
                label = calendarLabel,
                cornerRadius = cornerRadius,
                height = height
            ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    cornerRadius: Dp,
    height: Dp,
    onClick: () -> Unit
) {
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val visuals = remember(label) { quickActionVisuals(label) }

    Box(
        modifier = modifier
            .height(height)
            .shadow(
                elevation = 18.dp,
                shape = shape,
                clip = false,
                ambientColor = visuals.glowColor.copy(alpha = 0.25f),
                spotColor = visuals.glowColor.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(visuals.background, shape)
            .border(1.25.dp, visuals.border, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(visuals.tint, shape)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height * 0.45f)
                .align(Alignment.TopCenter)
                .background(
                    brush = visuals.highlight,
                    shape = RoundedCornerShape(
                        topStart = cornerRadius,
                        topEnd = cornerRadius,
                        bottomEnd = 0.dp,
                        bottomStart = 0.dp
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(visuals.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visuals.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 0.5.dp)
            )
        }
    }
}

private data class QuickActionVisuals(
    val icon: ImageVector,
    val background: Brush,
    val border: Brush,
    val highlight: Brush,
    val tint: Color,
    val iconBackground: Brush,
    val glowColor: Color
)

private fun quickActionVisuals(label: String): QuickActionVisuals {
    val (icon, colors) = when (label.lowercase()) {
        "notes" -> Icons.Filled.Description to listOf(Color(0xFF6DD5FA), Color(0xFF2980B9))
        "goals" -> Icons.Filled.Flag to listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4))
        "events" -> Icons.Filled.Event to listOf(Color(0xFF74EBD5), Color(0xFFACB6E5))
        else -> Icons.Filled.DateRange to listOf(Color(0xFFFBC2EB), Color(0xFFA6C1EE))
    }

    val background = Brush.linearGradient(colors)
    val border = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.8f),
            Color.White.copy(alpha = 0.25f)
        ),
        tileMode = TileMode.Clamp
    )
    val highlight = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.28f),
            Color.White.copy(alpha = 0.04f)
        )
    )
    val tint = Color.White.copy(alpha = 0.08f)
    val iconBackground = Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.08f))
    )

    return QuickActionVisuals(
        icon = icon,
        background = background,
        border = border,
        highlight = highlight,
        tint = tint,
        iconBackground = iconBackground,
        glowColor = colors.last()
    )
}
