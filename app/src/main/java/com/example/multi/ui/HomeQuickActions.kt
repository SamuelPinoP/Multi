package com.example.multi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.MedallionSegment

private data class QuickAction(
    val segment: MedallionSegment,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val colors: List<Color>
)

@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    onActionClick: (MedallionSegment) -> Unit
) {
    val actions = remember {
        listOf(
            QuickAction(
                segment = MedallionSegment.NOTES,
                label = "Notes",
                description = "Capture thoughts fast",
                icon = Icons.Default.Note,
                colors = listOf(Color(0xFF3C8CE7), Color(0xFF00EAFF))
            ),
            QuickAction(
                segment = MedallionSegment.WEEKLY_GOALS,
                label = "Goals",
                description = "Track weekly wins",
                icon = Icons.Default.Flag,
                colors = listOf(Color(0xFF2AF598), Color(0xFF009EFD))
            ),
            QuickAction(
                segment = MedallionSegment.EVENTS,
                label = "Events",
                description = "Plan every moment",
                icon = Icons.Default.Event,
                colors = listOf(Color(0xFFFF5858), Color(0xFFF09819))
            ),
            QuickAction(
                segment = MedallionSegment.CALENDAR,
                label = "Calendar",
                description = "See the big picture",
                icon = Icons.Default.DateRange,
                colors = listOf(Color(0xFFA18CD1), Color(0xFFFBC2EB))
            )
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            actions.take(2).forEach { action ->
                QuickActionCard(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 120.dp),
                    action = action,
                    onClick = onActionClick
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            actions.drop(2).forEach { action ->
                QuickActionCard(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 120.dp),
                    action = action,
                    onClick = onActionClick
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    action: QuickAction,
    onClick: (MedallionSegment) -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        val gradient = remember(action.colors) {
            Brush.linearGradient(action.colors)
        }
        Column(
            modifier = Modifier
                .background(gradient, RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick(action.segment) }
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = action.label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 0.2.sp
            )
        }
    }
}
