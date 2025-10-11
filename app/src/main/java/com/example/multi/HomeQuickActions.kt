package com.example.multi

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.NoteAlt
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private data class QuickActionDefinition(
    val segment: MedallionSegment,
    @StringRes val labelRes: Int,
    @StringRes val subtitleRes: Int,
    val gradient: List<Color>,
)

@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    onActionClick: (MedallionSegment) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val quickActions = listOf(
        QuickActionDefinition(
            segment = MedallionSegment.NOTES,
            labelRes = R.string.label_notes,
            subtitleRes = R.string.quick_action_notes_tagline,
            gradient = listOf(colorScheme.primary, colorScheme.primaryContainer.copy(alpha = 0.85f)),
        ),
        QuickActionDefinition(
            segment = MedallionSegment.WEEKLY_GOALS,
            labelRes = R.string.label_weekly_goals,
            subtitleRes = R.string.quick_action_goals_tagline,
            gradient = listOf(colorScheme.secondaryContainer, colorScheme.tertiary.copy(alpha = 0.9f)),
        ),
        QuickActionDefinition(
            segment = MedallionSegment.EVENTS,
            labelRes = R.string.label_events,
            subtitleRes = R.string.quick_action_events_tagline,
            gradient = listOf(colorScheme.tertiaryContainer, colorScheme.secondary.copy(alpha = 0.9f)),
        ),
        QuickActionDefinition(
            segment = MedallionSegment.CALENDAR,
            labelRes = R.string.label_calendar,
            subtitleRes = R.string.quick_action_calendar_tagline,
            gradient = listOf(colorScheme.secondary, colorScheme.inversePrimary.copy(alpha = 0.9f)),
        ),
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        quickActions.forEach { action ->
            QuickActionCard(
                definition = action,
                modifier = Modifier.weight(1f),
                onClick = { onActionClick(action.segment) },
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    definition: QuickActionDefinition,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.height(112.dp),
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(definition.gradient),
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = definition.gradient.first().copy(alpha = 0.35f),
                ) {
                    val icon = when (definition.segment) {
                        MedallionSegment.NOTES -> Icons.Rounded.NoteAlt
                        MedallionSegment.WEEKLY_GOALS -> Icons.Rounded.Flag
                        MedallionSegment.EVENTS -> Icons.Rounded.Event
                        MedallionSegment.CALENDAR -> Icons.Rounded.CalendarMonth
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp),
                    )
                }

                Column {
                    Text(
                        text = stringResource(id = definition.labelRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = definition.subtitleRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        }
    }
}
