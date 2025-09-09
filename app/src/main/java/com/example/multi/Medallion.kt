package com.example.multi

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Motion tuning */
private object Motion {
    const val WordmarkShiftMs = 2400   // gradient sweep
    const val SparkleMs = 3000         // underline sparkle (slower ball)
    const val TapScaleMs = 120         // wordmark tap effect
    const val CardPressMs = 120        // card press scale

    // Orbit motion (hold)
    const val SpinStepMs = 70          // delay between angle steps while holding (bigger = slower)
    const val SpinStepDeg = 3f         // degrees added per step (smaller = smoother/slower)
}

/** Enum describing each clickable segment of the medallion. */
enum class MedallionSegment { WEEKLY_GOALS, CALENDAR, EVENTS, NOTES }

private data class SegmentDefinition(
    val segment: MedallionSegment,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val containerColor: Color
)

/** Ambient animated background with soft moving blobs */
@Composable
private fun AnimatedBackdrop(modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val infinite = rememberInfiniteTransition(label = "bg")
    val shiftA by infinite.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(
            tween(9000, easing = LinearEasing), RepeatMode.Reverse
        ),
        label = "bgA"
    )
    val shiftB by infinite.animateFloat(
        1f, 0f,
        animationSpec = infiniteRepeatable(
            tween(11000, easing = LinearEasing), RepeatMode.Reverse
        ),
        label = "bgB"
    )
    Canvas(modifier.fillMaxSize()) {
        drawRect(color = c.surface) // base

        val w = size.width
        val h = size.height

        fun blob(centerX: Float, centerY: Float, color: Color, radius: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = radius
                ),
                radius = radius,
                center = Offset(centerX, centerY),
                blendMode = BlendMode.SrcOver
            )
        }

        blob(w * (0.25f + 0.1f * shiftA), h * 0.2f, c.primary, w * 0.6f)
        blob(w * (0.85f - 0.1f * shiftB), h * 0.75f, c.tertiary, w * 0.7f)
        blob(w * 0.5f, h * (0.5f + 0.05f * (shiftA - shiftB)), c.secondary, w * 0.55f)

        drawLine(
            brush = Brush.horizontalGradient(listOf(c.primary.copy(0.2f), Color.Transparent)),
            start = Offset(0f, 0f),
            end = Offset(w, 0f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/** Wordmark with animated gradient + slower sparkle bar */
@Composable
private fun MultiWordmark(
    modifier: Modifier = Modifier,
    title: String = "Multi",
    onClick: (() -> Unit)? = null
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.06f else 1f,
        animationSpec = tween(durationMillis = Motion.TapScaleMs, easing = LinearEasing),
        label = "wordmarkScale"
    )

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

        Text(
            text = "Notes • Goals • Events • Calendar",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(160.dp)
        ) {
            Canvas(Modifier.matchParentSize()) {
                val radius = size.height / 2f
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            c.primary.copy(alpha = 0.22f),
                            c.secondary.copy(alpha = 0.22f),
                            c.tertiary.copy(alpha = 0.22f)
                        )
                    ),
                    cornerRadius = CornerRadius(radius, radius)
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    size = Size(width = size.width, height = size.height / 2.2f),
                    cornerRadius = CornerRadius(radius, radius)
                )
                val x = sparkX * size.width
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = size.height * 0.45f,
                    center = Offset(x, size.height / 2f)
                )
            }
        }
    }
}

/** Card for each segment with gradient border + press scale + soft gloss */
@Composable
private fun SegmentCard(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    square: Boolean = true,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(Motion.CardPressMs, easing = LinearEasing),
        label = "cardScale"
    )
    val contentColor = contentColorFor(containerColor)
    val shape = RoundedCornerShape(16.dp)

    val cardModifier = (if (square) modifier.aspectRatio(1f) else modifier)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
                )
            ),
            shape = shape
        )
        .semantics { contentDescription = label }
        .drawBehind {
            drawRoundRect(
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(0.09f), Color.Transparent)
                ),
                size = Size(size.width, size.height / 2.6f),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
            drawRoundRect(
                color = Color.White.copy(0.06f),
                style = Stroke(width = 1.dp.toPx()),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
        }

    ElevatedCard(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        shape = shape,
        modifier = cardModifier,
        interactionSource = interaction
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
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

    // Which segment sits at which base slot (top/right/bottom/left). We shuffle this on wordmark tap.
    var order by rememberSaveable {
        mutableStateOf(
            listOf(
                MedallionSegment.NOTES,
                MedallionSegment.WEEKLY_GOALS,
                MedallionSegment.EVENTS,
                MedallionSegment.CALENDAR
            )
        )
    }

    // Current orbit angle (degrees). Increases clockwise while holding.
    var angleDeg by rememberSaveable { mutableStateOf(0f) }
    var spinning by remember { mutableStateOf(false) }

    // Compose scope for launching coroutines from pointer input
    val scope = rememberCoroutineScope()
    val viewConfig = LocalViewConfiguration.current
    val longPressMs = viewConfig.longPressTimeoutMillis

    Box(
        modifier = modifier
            .fillMaxSize()
            // Long-press anywhere to start orbit; release to stop.
            .pointerInput(longPressMs) {
                while (true) {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        val starter = scope.launch {
                            delay(longPressMs)
                            spinning = true
                            launch {
                                while (true) {
                                    angleDeg = (angleDeg + Motion.SpinStepDeg) % 360f
                                    delay(Motion.SpinStepMs.toLong())
                                }
                            }
                        }
                        // Wait until all pointers are up
                        var done = false
                        while (!done) {
                            val ev = awaitPointerEvent()
                            val allUp = ev.changes.all { !it.pressed }
                            if (allUp) done = true
                        }
                        spinning = false
                        starter.cancel()
                    }
                }
            }
    ) {
        // Ambient animated background
        AnimatedBackdrop(Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TAP on the wordmark = random shuffle of the four slots (kept behavior)
            MultiWordmark(
                modifier = Modifier.padding(top = 8.dp),
                onClick = { order = order.shuffled() }
            )

            Text(
                text = "Your productivity hub",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 🟡 Circular ring layout (no BoxWithConstraints; no lint warning)
            val density = LocalDensity.current
            var containerSize by remember { mutableStateOf(0.dp) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .onSizeChanged { sz ->
                        val minPx = min(sz.width, sz.height)
                        containerSize = with(density) { minPx.toDp() }
                    }
            ) {
                if (containerSize > 0.dp) {
                    val size = containerSize
                    val cardSize = (size * 0.38f).coerceIn(96.dp, 160.dp)
                    val radius = size / 2 - cardSize / 2 - 8.dp

                    val base = listOf(270f, 0f, 90f, 180f) // top, right, bottom, left (clockwise)

                    Box(Modifier.fillMaxSize()) {
                        order.forEachIndexed { index, seg ->
                            val def = definitions.getValue(seg)
                            val theta = (base[index] + angleDeg) * (PI / 180f)
                            val dx = radius * cos(theta).toFloat()
                            val dy = radius * sin(theta).toFloat()

                            Box(
                                modifier = Modifier
                                    .size(cardSize)
                                    .align(Alignment.Center)
                                    .offset(x = dx, y = dy)
                            ) {
                                SegmentCard(
                                    label = stringResource(def.labelRes),
                                    icon = def.icon,
                                    containerColor = def.containerColor,
                                    enabled = !spinning,
                                    onClick = { if (!spinning) onSegmentClick(def.segment) },
                                    modifier = Modifier.fillMaxSize(),
                                    square = false
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Simple screen displaying the [Medallion] in the center. */
@Composable
fun MedallionScreen() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
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
}
