package com.example.multi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ModernButtonColors(
    val gradient: List<Color>,
    val onContent: Color,
    val border: Color,
    val highlight: Color
)

object ModernButtonDefaults {
    fun notes(): ModernButtonColors = ModernButtonColors(
        gradient = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
        onContent = Color.White,
        border = Color.White.copy(alpha = 0.55f),
        highlight = Color(0xFFB9A2FF)
    )

    fun goals(): ModernButtonColors = ModernButtonColors(
        gradient = listOf(Color(0xFF00B09B), Color(0xFF96C93D)),
        onContent = Color.White,
        border = Color.White.copy(alpha = 0.55f),
        highlight = Color(0xFFBBF8C7)
    )

    fun events(): ModernButtonColors = ModernButtonColors(
        gradient = listOf(Color(0xFFFF512F), Color(0xFFF09819)),
        onContent = Color.White,
        border = Color.White.copy(alpha = 0.55f),
        highlight = Color(0xFFFFC48C)
    )

    fun calendar(): ModernButtonColors = ModernButtonColors(
        gradient = listOf(Color(0xFF36D1DC), Color(0xFF5B86E5)),
        onContent = Color.White,
        border = Color.White.copy(alpha = 0.55f),
        highlight = Color(0xFFAED4FF)
    )

    fun shape(): Shape = RoundedCornerShape(28.dp)
}

@Composable
fun ModernActionButton(
    modifier: Modifier = Modifier,
    label: String,
    colors: ModernButtonColors,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    shape: Shape = ModernButtonDefaults.shape(),
    shadowElevation: Dp = 16.dp,
    tonalElevation: Dp = 4.dp
) {
    val gradientBrush = remember(colors.gradient) {
        Brush.linearGradient(colors.gradient)
    }
    val highlightBrush = remember(colors.highlight) {
        Brush.verticalGradient(
            colors = listOf(colors.highlight.copy(alpha = 0.45f), Color.Transparent)
        )
    }

    Surface(
        modifier = modifier,
        onClick = onClick,
        color = Color.Transparent,
        shape = shape,
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation,
        border = BorderStroke(1.dp, colors.border)
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.onContent) {
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(gradientBrush)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(highlightBrush)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    if (icon != null) {
                        Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                            icon()
                        }
                    }

                    if (icon != null) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                    }

                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LocalContentColor.current,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
