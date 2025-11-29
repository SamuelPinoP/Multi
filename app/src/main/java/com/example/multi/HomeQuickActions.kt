package com.example.multi

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * A single-row bar of 4 sophisticated quick-action buttons with subtle animations
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    calendarLabel: String = "Calendar",
    cornerRadius: Dp = 26.dp,
    height: Dp = 124.dp,
    gap: Dp = 12.dp,
    borderWidth: Dp = 1.5.dp
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    val actions = listOf(
        QuickAction(
            label = "Notes",
            description = "Capture thoughts",
            colors = listOf(Color(0xFF9F7AEA), Color(0xFF7C3AED)),
            icon = Icons.Outlined.NoteAdd,
            onClick = { context.startActivity(Intent(context, NotesActivity::class.java)) }
        ),
        QuickAction(
            label = "Goals",
            description = "Stay on track",
            colors = listOf(Color(0xFF4ADE80), Color(0xFF22C55E)),
            icon = Icons.Outlined.CheckCircle,
            onClick = { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }
        ),
        QuickAction(
            label = "Events",
            description = "Plan ahead",
            colors = listOf(Color(0xFFFFA64D), Color(0xFFFF5F6D)),
            icon = Icons.Outlined.Event,
            onClick = { context.startActivity(Intent(context, EventsActivity::class.java)) }
        ),
        QuickAction(
            label = calendarLabel,
            description = "See your week",
            colors = listOf(Color(0xFF60A5FA), Color(0xFF3B82F6)),
            icon = Icons.Outlined.DateRange,
            onClick = { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { action ->
            SophisticatedButton(
                modifier = Modifier.weight(1f),
                label = action.label,
                subtitle = action.description,
                shape = shape,
                borderWidth = borderWidth,
                height = height,
                colors = action.colors,
                icon = action.icon,
                onClick = action.onClick
            )
        }
    }
}

private data class QuickAction(
    val label: String,
    val description: String,
    val colors: List<Color>,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun SophisticatedButton(
    modifier: Modifier = Modifier,
    label: String,
    subtitle: String,
    shape: RoundedCornerShape,
    borderWidth: Dp,
    height: Dp,
    colors: List<Color>,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale",
    )

    val textColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
        animationSpec = tween(150),
        label = "textColor",
    )

    var shimmerOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            shimmerOffset = (shimmerOffset + 220f) % 400f
            delay(1400)
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .drawWithContent {
                drawContent()

                val strokeWidth = borderWidth.toPx()
                val cornerRadiusPx = shape.topStart.toPx(size, this)

                val borderColors = listOf(
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.05f),
                    Color.White.copy(alpha = 0.25f)
                )

                val brush = Brush.sweepGradient(
                    colors = borderColors,
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
                Brush.linearGradient(
                    colors = colors,
                    start = Offset.Zero,
                    end = Offset(420f, 180f)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = label,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        color = textColor.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.22f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .matchParentSize()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.5f)),
                                start = Offset(shimmerOffset, 0f),
                                end = Offset(shimmerOffset + 80f, 40f)
                            )
                        )
                )
            }
        }
    }
}

