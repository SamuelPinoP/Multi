package com.example.multi

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class QuickAction(
    val segment: MedallionSegment,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val accent: Color,
    val gradient: List<Color>,
    val subtitle: String
)

@Composable
fun MedallionQuickActions(
    onSegmentSelected: (MedallionSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val actions = listOf(
        QuickAction(
            segment = MedallionSegment.NOTES,
            labelRes = R.string.label_notes,
            icon = Icons.Filled.Note,
            accent = colors.primary,
            gradient = listOf(colors.primary, colors.primaryContainer, colors.secondaryContainer),
            subtitle = stringResource(id = R.string.quick_action_notes)
        ),
        QuickAction(
            segment = MedallionSegment.WEEKLY_GOALS,
            labelRes = R.string.label_weekly_goals,
            icon = Icons.Filled.Flag,
            accent = colors.tertiary,
            gradient = listOf(colors.tertiary, colors.secondary, colors.surfaceVariant),
            subtitle = stringResource(id = R.string.quick_action_goals)
        ),
        QuickAction(
            segment = MedallionSegment.EVENTS,
            labelRes = R.string.label_events,
            icon = Icons.Filled.Event,
            accent = colors.error,
            gradient = listOf(colors.error, colors.errorContainer, colors.primaryContainer),
            subtitle = stringResource(id = R.string.quick_action_events)
        ),
        QuickAction(
            segment = MedallionSegment.CALENDAR,
            labelRes = R.string.label_calendar,
            icon = Icons.Filled.DateRange,
            accent = colors.secondary,
            gradient = listOf(colors.secondary, colors.inversePrimary, colors.surfaceVariant),
            subtitle = stringResource(id = R.string.quick_action_calendar)
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(32.dp), clip = false)
            .clip(RoundedCornerShape(32.dp))
            .background(colors.surface.copy(alpha = 0.82f))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(id = R.string.quick_actions_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onSurfaceVariant
        )
        val rows = actions.chunked(2)
        rows.forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowActions.forEach { action ->
                    QuickActionCard(
                        action = action,
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        onSegmentSelected(action.segment)
                    }
                }
                if (rowActions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val targetScale = if (isPressed) 0.97f else 1f
    val scale by animateFloatAsState(targetValue = targetScale, label = "quickActionScale")

    Card(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(action.gradient))
                .padding(vertical = 20.dp, horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(action.accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = action.icon,
                        contentDescription = stringResource(id = action.labelRes),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(id = action.labelRes),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
