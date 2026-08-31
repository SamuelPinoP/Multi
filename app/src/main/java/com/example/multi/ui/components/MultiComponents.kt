package com.example.multi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.multi.ui.theme.MultiTheme

/* ============================================================================
 * MultiCard — the one card used across every list in the app.
 *
 * A single tonal surface (no drop shadow — elevation is expressed with tone,
 * per the M3 guidance) with a consistent 16 dp radius and internal padding.
 * ========================================================================= */

@Composable
fun MultiCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    tonal: Boolean = false,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    val container = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        tonal -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    Surface(
        color = container,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (tonal) 2.dp else 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

/* ============================================================================
 * SectionHeader — small-caps label + optional trailing action, for grouping
 * content inside a screen.
 * ========================================================================= */

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MultiTheme.spacing.xs, vertical = MultiTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        trailing?.invoke()
    }
}

/* ============================================================================
 * Pill — a compact, rounded status/metadata chip. `accent` tints it; leave it
 * null for the neutral surface treatment.
 * ========================================================================= */

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color? = null,
    onAccent: Color? = null,
) {
    val container = accent ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val content = onAccent ?: MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/* ============================================================================
 * Avatar — a circular monogram / icon badge in a segment accent colour.
 * ========================================================================= */

@Composable
fun MonogramAvatar(
    label: String,
    container: Color,
    onContainer: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    Surface(
        color = container,
        contentColor = onContainer,
        shape = CircleShape,
        modifier = modifier.size(size),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.trim().take(1).uppercase().ifBlank { "•" },
                style = MaterialTheme.typography.titleMedium,
                color = onContainer,
            )
        }
    }
}

/* ============================================================================
 * StatValue — a big Space Grotesk number with a caption under it, for the
 * medallion labels and dashboards.
 * ========================================================================= */

@Composable
fun StatValue(
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Column(
        modifier = modifier.widthIn(min = 0.dp),
        horizontalAlignment = alignment,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.labelMedium,
            color = color.copy(alpha = 0.82f),
        )
    }
}

@Composable
fun RowScope.Weighted(content: @Composable () -> Unit) {
    Box(Modifier.weight(1f)) { content() }
}
