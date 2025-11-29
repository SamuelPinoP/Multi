package com.example.multi

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A single-row bar of modern quick-action buttons with subtle animations
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    calendarLabel: String = "Calendar",
    cornerRadius: Dp = 20.dp,
    height: Dp = 116.dp,
    gap: Dp = 12.dp
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(cornerRadius)

    val actions = listOf(
        QuickAction(
            label = "Notes",
            subtitle = "Capture thoughts",
            colors = listOf(Color(0xFF5D5FEF), Color(0xFF8A8CFF)),
            icon = Icons.Filled.Note
        ) { context.startActivity(Intent(context, NotesActivity::class.java)) },
        QuickAction(
            label = "Goals",
            subtitle = "Weekly focus",
            colors = listOf(Color(0xFF16C196), Color(0xFF2DD4BF)),
            icon = Icons.Filled.Flag
        ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) },
        QuickAction(
            label = "Events",
            subtitle = "Plan ahead",
            colors = listOf(Color(0xFFFF8D70), Color(0xFFFF5F6D)),
            icon = Icons.Filled.Event
        ) { context.startActivity(Intent(context, EventsActivity::class.java)) },
        QuickAction(
            label = calendarLabel,
            subtitle = "Your agenda",
            colors = listOf(Color(0xFF7C3AED), Color(0xFF9F7AEA)),
            icon = Icons.Filled.DateRange
        ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { action ->
            ModernQuickAction(
                modifier = Modifier.weight(1f),
                action = action,
                shape = shape,
                height = height
            )
        }
    }
}

@Composable
private fun ModernQuickAction(
    modifier: Modifier = Modifier,
    action: QuickAction,
    shape: RoundedCornerShape,
    height: Dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 220, easing = LinearEasing),
        label = "scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0.1f,
        animationSpec = tween(220),
        label = "glow"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing)
        ),
        label = "wave"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isPressed) Color.White else Color(0xFFF5F5FF),
        animationSpec = tween(180),
        label = "iconTint"
    )

    val cardElevation by animateFloatAsState(
        targetValue = if (isPressed) 8f else 14f,
        animationSpec = tween(200),
        label = "elevation"
    )

    Surface(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                shadowElevation = cardElevation
                shape = shape
                clip = true
            }
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = action.onClick
            ),
        shape = shape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = action.colors,
                        tileMode = TileMode.Clamp
                    )
                )
                .drawBehind {
                    val waveBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    )
                    val width = size.width
                    val heightPx = size.height
                    val offset = width * waveOffset
                    drawRect(
                        brush = waveBrush,
                        topLeft = androidx.compose.ui.geometry.Offset(x = -width + offset, y = 0f),
                        size = androidx.compose.ui.geometry.Size(width * 2, heightPx)
                    )
                }
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = action.label,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = action.subtitle,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = glowAlpha)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = iconTint
                    )
                }
            }
        }
    }
}

private data class QuickAction(
    val label: String,
    val subtitle: String,
    val colors: List<Color>,
    val icon: ImageVector,
    val onClick: () -> Unit
)
