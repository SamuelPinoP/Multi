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
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Tweak these to control overall speed. */
private object Motion {
    const val WordmarkShiftMs = 2400   // gradient sweep (↓ is faster)
    const val SparkleMs = 3000         // underline sparkle
    const val TapScaleMs = 120         // tap "breathe" time
    const val ItemMoveMs = 150         // grid item swap animation
}

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
    // Faster tap-to-breathe scale
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.06f else 1f,
        animationSpec = tween(durationMillis = Motion.TapScaleMs, easing = LinearEasing),
        label = "wordmarkScale"
    )

    // Faster animated gradient + sparkle
    val infinite = rememberInfiniteTransition(label = "wordmarkAnim")
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = Motion.WordmarkShiftMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shift"
    )
    val sparkX by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(Motion.SparkleMs, easing = LinearEasing),
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
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
            style = MaterialTheme.typography.displaySmall.copy(
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

        // Underline with faster sparkle
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
                // Moving sparkle dot (faster)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Medallion(
    modifier: Modifier = Modifier,
    onSegmentClick: (MedallionSegment) -> Unit = {}
) {
    // Build definitions immediately (safe on first composition)
    val c = MaterialTheme.colorScheme
    val definitions = remember(c) {
        mapOf(
            MedallionSegment.NOTES to SegmentDefinition(
                segment = MedallionSegment.NOTES,
                labelRes = R.string.label_notes,
                icon = Icons.Default.Note,
                containerColor = c.inversePrimary
            ),
            MedallionSegment.WEEKLY_GOALS to SegmentDefinition(
                segment = MedallionSegment.WEEKLY_GOALS,
                labelRes = R.string.label_weekly_goals,
                icon = Icons.Default.Flag,
                containerColor = c.primaryContainer
            ),
            MedallionSegment.EVENTS to SegmentDefinition(
                segment = MedallionSegment.EVENTS,
                labelRes = R.string.label_events,
                icon = Icons.Default.Event,
                containerColor = c.tertiaryContainer
            ),
            MedallionSegment.CALENDAR to SegmentDefinition(
                segment = MedallionSegment.CALENDAR,
                labelRes = R.string.label_calendar,
                icon = Icons.Default.DateRange,
                containerColor = c.secondaryContainer
            )
        )
    }

    // Keep just the ORDER as state; items will read definitions by key.
    var order by remember {
        mutableStateOf(
            listOf(
                MedallionSegment.NOTES,
                MedallionSegment.WEEKLY_GOALS,
                MedallionSegment.EVENTS,
                MedallionSegment.CALENDAR
            )
        )
    }

    // Faster rotation on tap
    fun reorder() {
        if (order.isEmpty()) return
        order = listOf(order.last()) + order.dropLast(1)
        // For random shuffle instead of rotation:
        // order = order.shuffled()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Clickable animated wordmark triggers reorder
        MultiWordmark(
            modifier = Modifier.padding(bottom = 8.dp),
            onClick = { order = order.shuffled() }
        )

        // Grid with faster item motion
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = order,
                key = { it } // stable key = enum itself
            ) { segment ->
                val def = definitions.getValue(segment)
                SegmentButton(
                    label = stringResource(def.labelRes),
                    icon = def.icon,
                    containerColor = def.containerColor,
                    onClick = { onSegmentClick(def.segment) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement(
                            animationSpec = tween(
                                durationMillis = Motion.ItemMoveMs,
                                easing = LinearEasing
                            )
                        )
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