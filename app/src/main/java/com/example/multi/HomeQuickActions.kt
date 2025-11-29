package com.example.multi

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

/**
 * A refreshed single-row bar of four modern quick-action buttons with subtle depth and glow.
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    calendarLabel: String = "Calendar",
    cornerRadius: Dp = 26.dp,
    height: Dp = 120.dp,
    gap: Dp = 10.dp,
    borderWidth: Dp = 1.5.dp
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)
    val containerShape = RoundedCornerShape(cornerRadius + 8.dp)

    val containerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
        ),
        start = Offset.Zero,
        end = Offset(640f, 920f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f))
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(containerShape)
                .background(containerBrush)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                    shape = containerShape
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = height),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SophisticatedButton(
                    modifier = Modifier.weight(1f),
                    label = "Notes",
                    shape = shape,
                    borderWidth = borderWidth,
                    height = height,
                    icon = Icons.Filled.Note,
                    accent = MaterialTheme.colorScheme.primary
                ) { context.startActivity(Intent(context, NotesActivity::class.java)) }

                SophisticatedButton(
                    modifier = Modifier.weight(1f),
                    label = "Goals",
                    shape = shape,
                    borderWidth = borderWidth,
                    height = height,
                    icon = Icons.Filled.Flag,
                    accent = MaterialTheme.colorScheme.tertiary
                ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

                SophisticatedButton(
                    modifier = Modifier.weight(1f),
                    label = "Events",
                    shape = shape,
                    borderWidth = borderWidth,
                    height = height,
                    icon = Icons.Filled.Event,
                    accent = MaterialTheme.colorScheme.secondary
                ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

                SophisticatedButton(
                    modifier = Modifier.weight(1f),
                    label = calendarLabel,
                    shape = shape,
                    borderWidth = borderWidth,
                    height = height,
                    icon = Icons.Filled.DateRange,
                    accent = MaterialTheme.colorScheme.inversePrimary
                ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
            }
        }
    }
}

@Composable
private fun SophisticatedButton(
    modifier: Modifier = Modifier,
    label: String,
    shape: RoundedCornerShape,
    borderWidth: Dp,
    height: Dp,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Subtle scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale",
    )

    // Animated border gradient (very subtle)
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle",
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .drawWithContent {
                drawContent()

                val strokeWidth = borderWidth.toPx()
                val cornerRadiusPx = shape.topStart.toPx(size, this)

                val pulseAlpha = 0.25f + 0.15f * abs(sin(Math.toRadians(animatedAngle.toDouble())).toFloat())
                val brush = Brush.sweepGradient(
                    colors = listOf(
                        accent.copy(alpha = pulseAlpha),
                        accent.copy(alpha = 0.18f),
                        accent.copy(alpha = pulseAlpha)
                    ),
                    center = Offset(size.width / 2, size.height / 2)
                )

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(
                        size.width - strokeWidth,
                        size.height - strokeWidth
                    ),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = strokeWidth)
                )
            }
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.26f),
                        accent.copy(alpha = 0.1f)
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isPressed) 0.9f else 0.78f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isPressed) 0.9f else 0.8f),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}
