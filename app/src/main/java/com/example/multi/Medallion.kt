package com.example.multi

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Enum describing each clickable segment of the medallion. */
enum class MedallionSegment { WEEKLY_GOALS, CALENDAR, EVENTS, NOTES }

private data class SegmentDefinition(
    val segment: MedallionSegment,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val containerColor: Color
)

/** Modern animated wordmark for "Multi". */
@Composable
private fun MultiWordmark(
    modifier: Modifier = Modifier,
    title: String = "Multi",
    onClick: (() -> Unit)? = null
) {
    // Tap-to-breathe scale
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 1.06f else 1f, label = "wordmarkScale")

    // Animated gradient + sparkle
    val infinite = rememberInfiniteTransition(label = "wordmarkAnim")
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shift"
    )
    val sparkX by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkX"
    )

    val c = MaterialTheme.colorScheme
    val fillBrush = Brush.linearGradient(
        colors = listOf(c.primary, c.secondary, c.tertiary, c.primary),
        start = Offset(shift, 0f),
        end = Offset(shift + 360f, 220f)
    )
    val tagline = stringResource(R.string.tagline_multi)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                val glowBrush = Brush.radialGradient(
                    colors = listOf(c.primary.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.maxDimension
                )
                drawCircle(
                    brush = glowBrush,
                    radius = size.maxDimension / 2f,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
            }
            .semantics { contentDescription = "Multi logo" }
            .then(
                if (onClick != null) Modifier.clickable {
                    pressed = !pressed
                    onClick()
                } else Modifier.clickable { pressed = !pressed }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main wordmark
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayLarge.copy(
                brush = fillBrush,
                fontWeight = FontWeight.ExtraBold,
                shadow = Shadow(
                    color = c.primary.copy(alpha = 0.35f),
                    offset = Offset(2f, 3f),
                    blurRadius = 10f
                )
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = tagline,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(
                color = c.onBackground.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Elegant underline with animated sparkle
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(140.dp)
        ) {
            Canvas(Modifier.matchParentSize()) {
                val radius = size.height / 2f
                // Base underline capsule
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            c.primary.copy(alpha = 0.22f),
                            c.secondary.copy(alpha = 0.22f),
                            c.tertiary.copy(alpha = 0.22f)
                        )
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
                )
                // Subtle inner highlight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = size.width,
                        height = size.height / 2.2f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
                )
                // Moving sparkle dot
                val x = sparkX * size.width
                drawCircle(
                    color = Color.White.copy(alpha = 0.82f),
                    radius = size.height * 0.45f,
                    center = Offset(x, size.height / 2f)
                )
            }
        }
    }
}

/**
 * Basic button used by [Medallion] segments.
 */
@Composable
private fun SegmentButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    square: Boolean = true
) {
    val cardModifier = if (square) {
        modifier.aspectRatio(1f)
    } else {
        modifier
    }
    val contentColor = contentColorFor(containerColor)
    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = cardModifier.semantics { contentDescription = label }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Displays the animated "Multi" wordmark and four clickable squares
 * representing calendar, events, weekly goals and notes.
 */
@Composable
fun Medallion(
    modifier: Modifier = Modifier,
    onSegmentClick: (MedallionSegment) -> Unit = {}
) {
    val segments = listOf(
        SegmentDefinition(
            segment = MedallionSegment.NOTES,
            labelRes = R.string.label_notes,
            icon = Icons.Default.Note,
            containerColor = MaterialTheme.colorScheme.inversePrimary
        ),
        SegmentDefinition(
            segment = MedallionSegment.WEEKLY_GOALS,
            labelRes = R.string.label_weekly_goals,
            icon = Icons.Default.Flag,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        SegmentDefinition(
            segment = MedallionSegment.EVENTS,
            labelRes = R.string.label_events,
            icon = Icons.Default.Event,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        SegmentDefinition(
            segment = MedallionSegment.CALENDAR,
            labelRes = R.string.label_calendar,
            icon = Icons.Default.DateRange,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // New animated wordmark
        MultiWordmark(modifier = Modifier.padding(bottom = 8.dp))

        // App sections grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(segments) { segment ->
                SegmentButton(
                    label = LocalContext.current.getString(segment.labelRes),
                    icon = segment.icon,
                    containerColor = segment.containerColor,
                    onClick = { onSegmentClick(segment.segment) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Simple screen displaying the [Medallion] in the center. */
@Composable
fun MedallionScreen() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Medallion { segment ->
            val cls = when (segment) {
                MedallionSegment.CALENDAR -> CalendarMenuActivity::class.java
                MedallionSegment.WEEKLY_GOALS -> WeeklyGoalsActivity::class.java
                MedallionSegment.EVENTS -> EventsActivity::class.java
                MedallionSegment.NOTES -> NotesActivity::class.java
            }
            context.startActivity(Intent(context, cls))
        }
    }
}

