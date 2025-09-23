package com.example.multi

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** Motion tuning */
private object Motion {
    const val WordmarkShiftMs = 2400
    const val TapScaleMs = 120
    const val SpinStepMs = 16
    const val SpinStepDeg = 3f
}

/** Visual tweaks */
private object Wheel {
    const val ShowDividers = false
    const val ArcEpsilonDeg = 0.8f
    const val WheelScale = 1.22f
}

/** Textured styles */
private object Lava { // EVENTS
    const val SpeedPxPerSec = 42f
    const val Scale = 1.15f
    const val GlowStrength = 0.55f
    const val TextOnRimBias = 0.82f
    @DrawableRes val BitmapRes: Int = R.drawable.red_tile
}
private object Ice { // NOTES
    const val SpeedPxPerSec = 22f
    const val Scale = 1.0f
    const val GlowStrength = 0.45f
    const val TextOnRimBias = 0.80f
    @DrawableRes val BitmapRes: Int = R.drawable.blue_tile
}
private object Rock { // CALENDAR
    const val SpeedPxPerSec = 16f
    const val Scale = 1.0f
    const val GlowStrength = 0.35f
    const val TextOnRimBias = 0.80f
    @DrawableRes val BitmapRes: Int = R.drawable.gray_tile
}
private object Moss { // WEEKLY GOALS
    const val SpeedPxPerSec = 14f     // very calm
    const val Scale = 1.1f            // slightly enlarged fibers
    const val GlowStrength = 0.40f    // soft sunlit shimmer
    const val TextOnRimBias = 0.82f
    @DrawableRes val BitmapRes: Int = R.drawable.green_tile
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
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgA"
    )
    val shiftB by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgB"
    )
    Canvas(modifier.fillMaxSize()) {
        drawRect(color = c.surface)
        val w = size.width; val h = size.height
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
        label = "wmScale"
    )
    val infinite = rememberInfiniteTransition(label = "wm")
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = Motion.WordmarkShiftMs, easing = LinearEasing)
        ),
        label = "wmShift"
    )
    val c = MaterialTheme.colorScheme
    val fill = Brush.linearGradient(
        colors = listOf(c.primary, c.secondary, c.tertiary, c.primary),
        start = Offset(shift, 0f),
        end = Offset(shift + 360f, 220f)
    )

    Column(
        modifier = modifier.fillMaxWidth()
            .semantics { contentDescription = "Multi logo" }
            .then(if (onClick != null) Modifier.clickable { pressed = !pressed; onClick() } else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall.copy(
                brush = fill,
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
            "Notes • Goals • Events • Calendar",
            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

/** Build path for a pie-slice arc (center + wedge) */
private fun arcPath(rect: Rect, startDeg: Float, sweepDeg: Float) = Path().apply {
    moveTo(rect.center.x, rect.center.y); arcTo(rect, startDeg, sweepDeg, false); close()
}

/** Draw a scrolling tiled image clipped to wedge; add colored glow */
private fun DrawScope.drawTexturedSlice(
    bitmap: ImageBitmap,
    start: Float,
    sweep: Float,
    rect: Rect,
    scale: Float,
    phasePx: Float,
    glowColor: Color,
    glowStrength: Float,
    center: Offset,
    rPx: Float,
    dxFactor: Float = 0.55f,
    dyFactor: Float = 0.35f
) {
    val wedge = arcPath(rect, start, sweep)
    clipPath(wedge) {
        val rawW = bitmap.width.toFloat()
        val rawH = bitmap.height.toFloat()
        withTransform({
            translate(left = -phasePx * dxFactor, top = -phasePx * dyFactor)
            scale(scale, scale)
        }) {
            val cols = (size.width / (rawW * scale) + 3).toInt()
            val rows = (size.height / (rawH * scale) + 3).toInt()
            for (yy in -1..rows) for (xx in -1..cols) {
                drawImage(bitmap, topLeft = Offset(xx * rawW, yy * rawH))
            }
        }
        // additive glow/shimmer
        drawArc(
            brush = Brush.radialGradient(
                listOf(glowColor.copy(alpha = 0.18f * glowStrength), Color.Transparent),
                center = center, radius = rPx * 0.95f
            ),
            startAngle = start, sweepAngle = sweep, useCenter = true,
            topLeft = rect.topLeft, size = rect.size, blendMode = BlendMode.Plus
        )
    }
    // subtle rim highlight
    drawArc(
        brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)),
        startAngle = start, sweepAngle = sweep, useCenter = false,
        topLeft = rect.topLeft, size = rect.size, style = Stroke(width = rPx * 0.02f)
    )
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
            MedallionSegment.NOTES to SegmentDefinition(MedallionSegment.NOTES, R.string.label_notes, Icons.Default.Note, c.inversePrimary),
            MedallionSegment.WEEKLY_GOALS to SegmentDefinition(MedallionSegment.WEEKLY_GOALS, R.string.label_weekly_goals, Icons.Default.Flag, c.primaryContainer),
            MedallionSegment.EVENTS to SegmentDefinition(MedallionSegment.EVENTS, R.string.label_events, Icons.Default.Event, c.tertiaryContainer),
            MedallionSegment.CALENDAR to SegmentDefinition(MedallionSegment.CALENDAR, R.string.label_calendar, Icons.Default.DateRange, c.secondaryContainer)
        )
    }

    var order by rememberSaveable {
        mutableStateOf(listOf(MedallionSegment.NOTES, MedallionSegment.WEEKLY_GOALS, MedallionSegment.EVENTS, MedallionSegment.CALENDAR))
    }

    // Wheel rotation (deg)
    var angleDeg by rememberSaveable { mutableStateOf(0f) }
    var spinning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val outlineRing: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val dividerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    // Assets + animation phases
    val lavaBitmap = ImageBitmap.imageResource(Lava.BitmapRes)
    val iceBitmap = ImageBitmap.imageResource(Ice.BitmapRes)
    val rockBitmap = ImageBitmap.imageResource(Rock.BitmapRes)
    val mossBitmap = ImageBitmap.imageResource(Moss.BitmapRes)

    val phases = rememberInfiniteTransition(label = "textures")
    val lavaPhase by phases.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000f / Lava.SpeedPxPerSec * 1000f).toInt().coerceAtLeast(6000),
                easing = LinearEasing
            )
        ),
        label = "lavaPhase"
    )
    val icePhase by phases.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000f / Ice.SpeedPxPerSec * 1000f).toInt().coerceAtLeast(7000),
                easing = LinearEasing
            )
        ),
        label = "icePhase"
    )
    val rockPhase by phases.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000f / Rock.SpeedPxPerSec * 1000f).toInt().coerceAtLeast(8000),
                easing = LinearEasing
            )
        ),
        label = "rockPhase"
    )
    val mossPhase by phases.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1000f / Moss.SpeedPxPerSec * 1000f).toInt().coerceAtLeast(8500),
                easing = LinearEasing
            )
        ),
        label = "mossPhase"
    )

    Box(
        modifier = modifier.fillMaxSize().pointerInput(Unit) {
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
                        if (ev.changes.all { !it.pressed }) done = true
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
            MultiWordmark(modifier = Modifier.padding(top = 8.dp), onClick = { if (!spinning) order = order.shuffled() })
            Spacer(Modifier.height(24.dp))

            val density = LocalDensity.current
            var containerDp by remember { mutableStateOf(0.dp) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, true)
                    .aspectRatio(1f)
                    .onSizeChanged { sz ->
                        val minPx = min(sz.width, sz.height)
                        containerDp = with(density) { minPx.toDp() }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (containerDp > 0.dp) {
                    val radiusDp: Dp = containerDp * Wheel.WheelScale / 2f
                    val diameterDp: Dp = radiusDp * 2f
                    val mids = listOf(270f, 0f, 90f, 180f)

                    Canvas(Modifier.size(diameterDp).semantics { contentDescription = "Multi wheel" }) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val rPx = with(density) { radiusDp.toPx() }
                        val rectTopLeft = Offset(center.x - rPx, center.y - rPx)
                        val rect = Rect(rectTopLeft, Size(rPx * 2f, rPx * 2f))

                        drawCircle(
                            color = outlineRing, radius = rPx, center = center,
                            style = Stroke(width = with(density) { 2.dp.toPx() })
                        )

                        repeat(4) { i ->
                            val seg = order[i]
                            val start = mids[i] + angleDeg - 45f - Wheel.ArcEpsilonDeg
                            val sweep = 90f + Wheel.ArcEpsilonDeg * 2f

                            when (seg) {
                                MedallionSegment.EVENTS -> drawTexturedSlice(
                                    bitmap = lavaBitmap, start = start, sweep = sweep, rect = rect,
                                    scale = Lava.Scale, phasePx = lavaPhase,
                                    glowColor = Color(0xFFFF7A00), glowStrength = Lava.GlowStrength,
                                    center = center, rPx = rPx
                                )
                                MedallionSegment.NOTES -> drawTexturedSlice(
                                    bitmap = iceBitmap, start = start, sweep = sweep, rect = rect,
                                    scale = Ice.Scale, phasePx = icePhase,
                                    glowColor = Color(0xFF6AD0FF), glowStrength = Ice.GlowStrength,
                                    center = center, rPx = rPx
                                )
                                MedallionSegment.CALENDAR -> drawTexturedSlice(
                                    bitmap = rockBitmap, start = start, sweep = sweep, rect = rect,
                                    scale = Rock.Scale, phasePx = rockPhase,
                                    glowColor = Color(0xFF7EC8A6), glowStrength = Rock.GlowStrength,
                                    center = center, rPx = rPx,
                                    dxFactor = 0.35f, dyFactor = 0.20f
                                )
                                MedallionSegment.WEEKLY_GOALS -> drawTexturedSlice(
                                    bitmap = mossBitmap, start = start, sweep = sweep, rect = rect,
                                    scale = Moss.Scale, phasePx = mossPhase,
                                    glowColor = Color(0xFF7CFF63), // fresh green highlight
                                    glowStrength = Moss.GlowStrength,
                                    center = center, rPx = rPx,
                                    dxFactor = 0.25f, dyFactor = 0.18f // barely moving
                                )
                            }
                        }

                        if (Wheel.ShowDividers) {
                            repeat(4) { i2 ->
                                val a = (mids[i2] + angleDeg) * (PI / 180f)
                                val end = Offset(center.x + rPx * cos(a).toFloat(), center.y + rPx * sin(a).toFloat())
                                drawLine(
                                    color = dividerColor, start = center, end = end,
                                    strokeWidth = with(density) { 1.5.dp.toPx() }
                                )
                            }
                        }

                        rotate(angleDeg) {
                            drawCircle(
                                brush = Brush.radialGradient(listOf(Color.White.copy(0.08f), Color.Transparent)),
                                radius = rPx * 0.7f, center = Offset(center.x, center.y - rPx * 0.15f)
                            )
                        }
                    }

                    // Overlay: icons/labels & tap detection
                    Box(Modifier.size(diameterDp)) {
                        repeat(4) { i ->
                            val seg = order[i]; val def = defs.getValue(seg)
                            val labelColor =
                                if (seg == MedallionSegment.EVENTS || seg == MedallionSegment.NOTES
                                    || seg == MedallionSegment.CALENDAR || seg == MedallionSegment.WEEKLY_GOALS
                                ) Color.White else contentColorFor(def.color)

                            val theta = (mids[i] + angleDeg) * (PI / 180f)
                            val radiusBias = when (seg) {
                                MedallionSegment.EVENTS -> Lava.TextOnRimBias
                                MedallionSegment.NOTES -> Ice.TextOnRimBias
                                MedallionSegment.CALENDAR -> Rock.TextOnRimBias
                                MedallionSegment.WEEKLY_GOALS -> Moss.TextOnRimBias
                            }
                            val labelRadius = radiusDp * radiusBias
                            val dx = labelRadius * cos(theta).toFloat()
                            val dy = labelRadius * sin(theta).toFloat()

                            Column(
                                modifier = Modifier.align(Alignment.Center).offset(x = dx, y = dy),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // textured slices show text only (no icon)
                                if (seg != MedallionSegment.EVENTS &&
                                    seg != MedallionSegment.NOTES &&
                                    seg != MedallionSegment.CALENDAR &&
                                    seg != MedallionSegment.WEEKLY_GOALS
                                ) {
                                    Icon(def.icon, null, tint = labelColor)
                                    Spacer(Modifier.height(4.dp))
                                }
                                Text(
                                    text = stringResource(def.labelRes),
                                    color = labelColor,
                                    style = if (seg == MedallionSegment.EVENTS || seg == MedallionSegment.NOTES
                                        || seg == MedallionSegment.CALENDAR || seg == MedallionSegment.WEEKLY_GOALS
                                    ) MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                    else MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        val densityHere = density
                        Box(
                            Modifier.matchParentSize().pointerInput(order, angleDeg, radiusDp) {
                                detectTapGestures { tap ->
                                    val w = size.width.toFloat(); val h = size.height.toFloat()
                                    val cx = w / 2f; val cy = h / 2f
                                    val dx = tap.x - cx; val dy = tap.y - cy
                                    val dist = sqrt(dx * dx + dy * dy)
                                    val rPx = with(densityHere) { radiusDp.toPx() }
                                    if (dist <= rPx) {
                                        var ang = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        if (ang < 0f) ang += 360f
                                        val local = (ang - angleDeg + 360f) % 360f
                                        val quad = (((local + 45f) / 90f).toInt()) % 4
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
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
