package com.example.multi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun DayButtonsRow(states: String, onClick: (Int) -> Unit) {
    val labels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        labels.forEachIndexed { index, label ->
            val scheme = MaterialTheme.colorScheme
            val state = states.getOrNull(index) ?: ' '
            val (gradientColors, borderColor, contentColor) = when (state) {
                'C' -> Triple(
                    listOf(scheme.primary, scheme.tertiary, scheme.secondary),
                    scheme.onPrimary.copy(alpha = 0.55f),
                    scheme.onPrimary
                )

                'M' -> Triple(
                    listOf(scheme.error, scheme.errorContainer, Color(0xFFF9707A)),
                    scheme.onError.copy(alpha = 0.4f),
                    scheme.onError
                )

                else -> Triple(
                    listOf(scheme.surfaceVariant, scheme.surface, scheme.surfaceVariant),
                    scheme.outline.copy(alpha = 0.35f),
                    scheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(elevation = 10.dp, shape = CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors))
                    .border(BorderStroke(1.dp, borderColor), CircleShape)
                    .clickable { onClick(index) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                )
                Text(
                    text = label,
                    color = contentColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
