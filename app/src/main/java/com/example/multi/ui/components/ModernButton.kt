package com.example.multi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A lightly opinionated button with a subtle gradient border and rounded shape
 * to modernize the app's primary call-to-actions.
 */
@Composable
fun ModernButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ModernButtonVariant = ModernButtonVariant.Primary,
    colors: ButtonColors = ModernButtonDefaults.colors(variant),
    shape: Shape = ModernButtonDefaults.shape,
    border: BorderStroke = ModernButtonDefaults.border(variant, enabled),
    elevation: ButtonElevation? = ModernButtonDefaults.elevation,
    contentPadding: PaddingValues = ModernButtonDefaults.contentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Variants offer subtle hue changes so buttons can have their own identity
 * while still sharing a common look.
 */
@Immutable
enum class ModernButtonVariant(internal val accent: (ColorScheme) -> Color) {
    Primary({ it.primary }),
    Secondary({ it.secondary }),
    Tertiary({ it.tertiary }),
    Danger({ it.error });
}

object ModernButtonDefaults {
    private const val BorderAlpha = 0.8f
    val shape: Shape = RoundedCornerShape(16.dp)
    val contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
    val elevation: ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = 3.dp,
        pressedElevation = 1.dp,
        focusedElevation = 5.dp,
        hoveredElevation = 6.dp,
        disabledElevation = 0.dp
    )

    @Composable
    fun colors(variant: ModernButtonVariant = ModernButtonVariant.Primary): ButtonColors {
        val colorScheme = MaterialTheme.colorScheme
        val accent = variant.accent(colorScheme)
        return ButtonDefaults.buttonColors(
            containerColor = colorScheme.surfaceColorAtElevation(3.dp),
            contentColor = accent,
            disabledContainerColor = colorScheme.surfaceColorAtElevation(1.dp),
            disabledContentColor = colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }

    @Composable
    fun border(
        variant: ModernButtonVariant = ModernButtonVariant.Primary,
        enabled: Boolean = true
    ): BorderStroke {
        val accent = variant.accent(MaterialTheme.colorScheme)
        val gradient = if (enabled) {
            Brush.linearGradient(
                listOf(
                    accent.copy(alpha = BorderAlpha),
                    accent.copy(alpha = BorderAlpha * 0.6f),
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
        }

        return BorderStroke(1.5.dp, gradient)
    }
}
