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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Motion tuning */
private object Motion {
    const val WordmarkShiftMs = 2400
    const val TapScaleMs = 120
    const val SpinStepMs = 16
    const val SpinStepDeg = 3f
}

/** Visual & layout knobs (✅ tweak here) */
private object Wheel {
    const val ShowDividers = false       // true = draw center spokes
    const val ArcEpsilonDeg = 0.8f       // tiny overlap to hide seams
    const val BaseScale = 0.96f          // overall fit inside its square container (0.90..1.00)

    // 🔧 NEW: Move the wheel center up/down. Positive = move DOWN.
    val CenterShiftY: Dp = 190.dp

    // 🔧 NEW: Ellipse scaling. 1.0 = perfect circle
    // Increase ScaleY (keep ScaleX=1) to make it taller than wide.
    // Increase ScaleX (keep ScaleY=1) to make it wider than tall.
    const val ScaleX = 2.50f
    const val ScaleY = 2.50f
}

/** Enum describing each clickable slice. */
enum class MedallionSegment { WEEKLY_GOALS, CALENDAR, EVENTS, NOTES }

private data class SegmentDefinition(
    val segment: MedallionSegment,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val color: Color
)

/** Soft animated backdrop */
@Composable
private fun AnimatedBackdrop(modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val infinite = rememberInfiniteTransition(label = "bg")
    val shiftA by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bgA"
    )
    val shiftB by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bgB"
    )
    Canvas(modifier.fillMaxSize()) {
        drawRect(color = c.surface)
        val w = size.width
        val h = size.height
        fun blob(cx: Float, cy: Float, color: Color, r: Float) {
            drawCircle(
                brush = Brush.radialGradient(listOf(color.copy(alpha = 0.25f), Color.Transparent)),
                radius = r,
                center = Offset(cx, cy),
                blendMode = BlendMode.SrcOver
            )
        }
        blob(w * (0.25f + 0.1f * shiftA), h * 0.2f, c.primary, w * 0.6f)
        blob(w * (0.85f - 0.1f * shiftB), h * 0.75f, c.tertiary, w * 0.7f)
        blob(w * 0.5f, h * (0.5f + 0.05f * (shiftA - shiftB)), c.secondary, w * 0.55f)
    }
}

/** Gradient wordmark */
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
        animationSpec = infiniteRepeatable(tween(Motion.WordmarkShiftMs, easing = LinearEasing)),
        label = "shift"
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
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .semantics { contentDescription = "Multi logo" }
            .then(if (onClick != null) Modifier.clickable { pressed = !pressed; onClick() } else Modifier),
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
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Notes • Goals • Events • Calendar",
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Medallion(
    modifier: Modifier = Modifier,
    onSegmentClick: (MedallionSegment) -> Unit = {}
) {
    val c = MaterialTheme.colorScheme
    val defs = remember(c) {
        mapOf(
            MedallionSegment.NOTES to SegmentDefinition(
                MedallionSegment.NOTES, R.string.label_notes, Icons.Default.Note, c.inversePrimary
            ),
            MedallionSegment.WEEKLY_GOALS to SegmentDefinition(
                MedallionSegment.WEEKLY_GOALS, R.string.label_weekly_goals, Icons.Default.Flag, c.primaryContainer
            ),
            MedallionSegment.EVENTS to SegmentDefinition(
                MedallionSegment.EVENTS, R.string.label_events, Icons.Default.Event, c.tertiaryContainer
            ),
            MedallionSegment.CALENDAR to SegmentDefinition(
                MedallionSegment.CALENDAR, R.string.label_calendar, Icons.Default.DateRange, c.secondaryContainer
            )
        )
    }

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

    // Wheel rotation (deg)
    var angleDeg by rememberSaveable { mutableStateOf(0f) }
    var spinning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Colors for Canvas (no composable calls in draw lambda)
    val outlineRing: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val dividerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = true)
                        spinning = true
                        val spinner = scope.launch {
                            while (true) {
                                angleDeg = (angleDeg + Motion.SpinStepDeg) % 360f
                                delay(Motion.SpinStepMs.toLong())
                            }
                        }
                        var done = false
                        while (!done) {
                            val ev = awaitPointerEvent()
                            val allUp = ev.changes.all { !it.pressed }
                            if (allUp) done = true
                        }
                        spinning = false
                        spinner.cancel()
                    }
                }
            }
    ) {
        AnimatedBackdrop(Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            MultiWordmark(
                modifier = Modifier.padding(top = 8.dp),
                onClick = { if (!spinning) order = order.shuffled() }
            )

            Spacer(Modifier.height(24.dp))

            val density = LocalDensity.current
            var containerDp by remember { mutableStateOf(0.dp) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .aspectRatio(1f)
                    .onSizeChanged { sz ->
                        val minPx = min(sz.width, sz.height)
                        containerDp = with(density) { minPx.toDp() }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (containerDp > 0.dp) {
                    // Base radius that fits into a square, then apply ellipse scales.
                    val baseRadiusDp: Dp = containerDp * Wheel.BaseScale / 2f
                    val aDp: Dp = baseRadiusDp * Wheel.ScaleX   // horizontal radius
                    val bDp: Dp = baseRadiusDp * Wheel.ScaleY   // vertical radius

                    val canvasWidthDp: Dp = aDp * 2f
                    val canvasHeightDp: Dp = bDp * 2f

                    val mids = listOf(270f, 0f, 90f, 180f)

                    // --- SLICES (Canvas) ---
                    Canvas(
                        modifier = Modifier
                            .size(width = canvasWidthDp, height = canvasHeightDp)
                            .offset(y = Wheel.CenterShiftY) // move center vertically
                            .semantics { contentDescription = "Multi wheel" }
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val aPx = with(density) { aDp.toPx() }
                        val bPx = with(density) { bDp.toPx() }
                        val rectTopLeft = Offset(center.x - aPx, center.y - bPx)
                        val rect = Rect(rectTopLeft, androidx.compose.ui.geometry.Size(aPx * 2, bPx * 2))

                        // Outline ellipse
                        drawOval(
                            color = outlineRing,
                            topLeft = rect.topLeft,
                            size = rect.size,
                            style = Stroke(width = with(density) { 2.dp.toPx() })
                        )

                        // Four arcs, with tiny overlap to hide seams
                        repeat(4) { i ->
                            val seg = order[i]
                            val color = defs.getValue(seg).color
                            val start = mids[i] + angleDeg - 45f - Wheel.ArcEpsilonDeg
                            val sweep = 90f + Wheel.ArcEpsilonDeg * 2f
                            drawArc(
                                color = color,
                                startAngle = start,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = rect.topLeft,
                                size = rect.size
                            )
                        }

                        // Optional spokes
                        if (Wheel.ShowDividers) {
                            repeat(4) { i ->
                                val a = (mids[i] + angleDeg) * (PI / 180f)
                                val end = Offset(
                                    center.x + aPx * cos(a).toFloat(),
                                    center.y + bPx * sin(a).toFloat()
                                )
                                drawLine(
                                    color = dividerColor,
                                    start = center,
                                    end = end,
                                    strokeWidth = with(density) { 1.5.dp.toPx() }
                                )
                            }
                        }

                        // Soft rotating highlight (scaled to ellipse)
                        rotate(angleDeg) {
                            drawOval(
                                brush = Brush.radialGradient(
                                    listOf(Color.White.copy(0.08f), Color.Transparent)
                                ),
                                topLeft = Offset(
                                    center.x - aPx * 0.7f,
                                    center.y - bPx * 0.85f // slightly above center
                                ),
                                size = androidx.compose.ui.geometry.Size(aPx * 1.4f, bPx * 1.1f)
                            )
                        }
                    }

                    // --- ICONS/LABELS + TAP HIT TEST (overlay) ---
                    Box(
                        Modifier
                            .size(width = canvasWidthDp, height = canvasHeightDp)
                            .offset(y = Wheel.CenterShiftY)
                    ) {
                        // Labels positioned on the ellipse
                        repeat(4) { i ->
                            val seg = order[i]
                            val def = defs.getValue(seg)
                            val labelColor = contentColorFor(def.color)
                            val theta = (mids[i] + angleDeg) * (PI / 180f)

                            val aLabel = aDp * 0.55f
                            val bLabel = bDp * 0.55f
                            val dx = aLabel * cos(theta).toFloat()
                            val dy = bLabel * sin(theta).toFloat()

                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(x = dx, y = dy),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(def.icon, contentDescription = null, tint = labelColor)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(def.labelRes),
                                    color = labelColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Tap inside ellipse -> open slice (consumes tap, so background doesn't spin)
                        val densityHere = density
                        Box(
                            Modifier
                                .matchParentSize()
                                .pointerInput(order, angleDeg, aDp, bDp) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        down.consume()
                                        val up = waitForUpOrCancellation()

                                        val w = size.width.toFloat()
                                        val h = size.height.toFloat()
                                        val cx = w / 2f
                                        val cy = h / 2f
                                        // Fall back to the down position if the up event was cancelled
                                        val pos = (up ?: down).position
                                        val dxPx = pos.x - cx
                                        val dyPx = pos.y - cy

                                        val aPx = with(densityHere) { aDp.toPx() }
                                        val bPx = with(densityHere) { bDp.toPx() }

                                        // Inside ellipse check
                                        val u = dxPx / aPx
                                        val v = dyPx / bPx
                                        if (u * u + v * v <= 1f) {
                                            // Angle on normalized unit circle
                                            var ang = Math.toDegrees(atan2(v.toDouble(), u.toDouble())).toFloat()
                                            if (ang < 0f) ang += 360f

                                            val local = (ang - angleDeg + 360f) % 360f
                                            val quad = (((local + 45f) / 90f).toInt()) % 4
                                            // Map quad(0=right,1=bottom,2=left,3=top) to order index(0=top,1=right,2=bottom,3=left)
                                            val mappedIndex = (quad + 1) % 4
                                            onSegmentClick(order[mappedIndex])
                                        }
                                    }
                                }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Whole screen scaffold */
@Composable
fun MedallionScreen() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
