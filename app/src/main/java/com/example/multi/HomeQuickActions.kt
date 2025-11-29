package com.example.multi

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A single-row bar of 4 sophisticated quick-action buttons with subtle animations
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    calendarLabel: String = "Calendar",
    cornerRadius: Dp = 22.dp,
    height: Dp = 112.dp,
    gap: Dp = 2.dp,
    borderWidth: Dp = 1.5.dp
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrushes = listOf(
        Brush.linearGradient(listOf(Color(0xFF6DDCFF), Color(0xFF7F60F9))),
        Brush.linearGradient(listOf(Color(0xFFFF8FB1), Color(0xFFF6D365))),
        Brush.linearGradient(listOf(Color(0xFF84FAB0), Color(0xFF8FD3F4))),
        Brush.linearGradient(listOf(Color(0xFFA6C0FE), Color(0xFFF68084)))
    )

    val backgroundBrushes = listOf(
        Brush.verticalGradient(listOf(Color(0xFFF7FAFF), Color(0xFFE7EEFF))),
        Brush.verticalGradient(listOf(Color(0xFFFFF5F7), Color(0xFFFFEFE1))),
        Brush.verticalGradient(listOf(Color(0xFFF4FFFB), Color(0xFFE6F9FF))),
        Brush.verticalGradient(listOf(Color(0xFFF8F4FF), Color(0xFFFFEEF2)))
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .padding(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SophisticatedButton(
            modifier = Modifier.weight(1f),
            label = "Notes",
            shape = shape,
            borderWidth = borderWidth,
            height = height,
            borderBrush = borderBrushes[0],
            backgroundBrush = backgroundBrushes[0]
        ) { context.startActivity(Intent(context, NotesActivity::class.java)) }

        SophisticatedButton(
            modifier = Modifier.weight(1f),
            label = "Goals",
            shape = shape,
            borderWidth = borderWidth,
            height = height,
            borderBrush = borderBrushes[1],
            backgroundBrush = backgroundBrushes[1]
        ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

        SophisticatedButton(
            modifier = Modifier.weight(1f),
            label = "Events",
            shape = shape,
            borderWidth = borderWidth,
            height = height,
            borderBrush = borderBrushes[2],
            backgroundBrush = backgroundBrushes[2]
        ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

        SophisticatedButton(
            modifier = Modifier.weight(1f),
            label = calendarLabel,
            shape = shape,
            borderWidth = borderWidth,
            height = height,
            borderBrush = borderBrushes[3],
            backgroundBrush = backgroundBrushes[3]
        ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
    }
}

@Composable
private fun SophisticatedButton(
    modifier: Modifier = Modifier,
    label: String,
    shape: RoundedCornerShape,
    borderWidth: Dp,
    height: Dp,
    borderBrush: Brush,
    backgroundBrush: Brush,
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
        label = "scale"
    )

    // Text color animation on press
    val textColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF1a1a1a) else Color(0xFF111827),
        animationSpec = tween(150),
        label = "textColor"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .drawWithContent {
                drawContent()

                // Draw sophisticated gradient border
                val strokeWidth = borderWidth.toPx()
                val cornerRadiusPx = shape.topStart.toPx(size, this)

                drawRoundRect(
                    brush = borderBrush,
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
            .background(backgroundBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
    }
}