package com.example.multi

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.weight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeQuickActionsRow(
    modifier: Modifier = Modifier,
    onActionSelected: (MedallionSegment) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val actions = remember(colorScheme) {
        listOf(
            QuickAction(
                segment = MedallionSegment.NOTES,
                labelRes = R.string.quick_action_notes,
                icon = Icons.Filled.Note,
                accentColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            QuickAction(
                segment = MedallionSegment.WEEKLY_GOALS,
                labelRes = R.string.quick_action_goals,
                icon = Icons.Filled.Flag,
                accentColor = colorScheme.tertiary,
                contentColor = colorScheme.onTertiary
            ),
            QuickAction(
                segment = MedallionSegment.EVENTS,
                labelRes = R.string.quick_action_events,
                icon = Icons.Filled.Event,
                accentColor = colorScheme.error,
                contentColor = colorScheme.onError
            ),
            QuickAction(
                segment = MedallionSegment.CALENDAR,
                labelRes = R.string.quick_action_calendar,
                icon = Icons.Filled.DateRange,
                accentColor = colorScheme.secondary,
                contentColor = colorScheme.onSecondary
            ),
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { action ->
            QuickActionButton(
                action = action,
                modifier = Modifier.weight(1f),
                onClick = { onActionSelected(action.segment) }
            )
        }
    }
}

private data class QuickAction(
    val segment: MedallionSegment,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val accentColor: Color,
    val contentColor: Color,
)

@Composable
private fun QuickActionButton(
    action: QuickAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val gradient = remember(action.accentColor) {
        Brush.linearGradient(
            colors = listOf(
                action.accentColor.copy(alpha = 0.95f),
                action.accentColor.copy(alpha = 0.7f)
            )
        )
    }

    Box(
        modifier = modifier
            .heightIn(min = 96.dp)
            .shadow(12.dp, shape, clip = false)
            .clip(shape)
            .background(gradient)
            .semantics { role = Role.Button }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = action.contentColor
            )
            Text(
                text = stringResource(id = action.labelRes),
                color = action.contentColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
