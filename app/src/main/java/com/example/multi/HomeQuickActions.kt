package com.example.multi

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.multi.ui.theme.QuickActionBorderDark
import com.example.multi.ui.theme.QuickActionBorderLight
import com.example.multi.ui.theme.QuickActionGradientEnd
import com.example.multi.ui.theme.QuickActionGradientStart
import com.example.multi.ui.theme.QuickActionText

/**
 * A single-row bar of 4 sophisticated quick-action buttons with subtle animations
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    calendarLabel: String = "Calendar",
    cornerRadius: Dp = 22.dp,
    height: Dp = 92.dp,
    gap: Dp = 10.dp,
    borderWidth: Dp = 1.6.dp
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .padding(horizontal = 6.dp),
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
        label = "scale",
    )

    // Animated border gradient (very subtle)
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val animatedAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "angle",
    )

    // Text color animation on press
    val textColor by animateColorAsState(
        targetValue = if (isPressed) QuickActionText.copy(alpha = 0.92f) else QuickActionText,
        animationSpec = tween(140),
        label = "textColor",
    )

    val backgroundBrush = remember {
        Brush.verticalGradient(colors = listOf(QuickActionGradientStart, QuickActionGradientEnd))
    }

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(elevation = 10.dp, shape = shape, clip = false)
            .drawWithContent {
                drawContent()

                // Draw sophisticated gradient border
                val strokeWidth = borderWidth.toPx()
                val cornerRadiusPx = shape.topStart.toPx(size, this)

                val colors = listOf(
                    QuickActionBorderLight,
                    QuickActionBorderDark,
                    QuickActionBorderLight
                )

                val brush = Brush.sweepGradient(
                    colors = colors,
                    center = Offset(size.width / 2, size.height / 2)
                )

                rotate(animatedAngle) {
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
