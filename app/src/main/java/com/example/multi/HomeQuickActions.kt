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
            height = height
        ) { context.startActivity(Intent(context, NotesActivity::class.java)) }

        SophisticatedButton(
            modifier = Modifier.weight(1f),
            label = "Goals",
            shape = shape,
            borderWidth = borderWidth,
            height = height
        ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

        SophisticatedButton(
            modifier = Modifier.weight(1f),
            label = "Events",
            shape = shape,
            borderWidth = borderWidth,
            height = height
        ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

        SophisticatedButton(
            modifier = Modifier.weight(1f),
            label = calendarLabel,
            shape = shape,
            borderWidth = borderWidth,
            height = height
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

    // Animated border gradient (very subtle)
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    // Text color animation on press
    val textColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF1a1a1a) else Color(0xFF2a2a2a),
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

                // Create subtle gradient from dark gray to slightly lighter
                val colors = listOf(
                    Color(0xFF2a2a2a),
                    Color(0xFF404040),
                    Color(0xFF2a2a2a)
                )

                val brush = Brush.sweepGradient(
                    colors = colors,
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
            .background(Color.White)
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