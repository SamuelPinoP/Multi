package com.example.multi

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.multi.data.EventEntity
import com.example.multi.data.toModel
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt

import androidx.compose.runtime.mutableIntStateOf
import com.example.multi.data.EventDatabase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Motion tuning */
private object Motion {
    const val WordmarkShiftMs = 2400
    const val TapScaleMs = 120
    const val SpinStepMs = 16
    const val SpinStepDeg = 3f
}

/** Visual tweaks */
private object Wheel {
    // tiny overlap to hide AA hairlines at slice boundaries (not image seams)
    const val ArcEpsilonDeg = 2.0f
    const val WheelScale = 1.20f
}

/** Texture sets (put your images in res/drawable as *_tile_1..10.png) */
private object Lava { // EVENTS (red)
    const val Scale = 1.15f        // ≈115% zoom
    const val GlowStrength = 0.55f
    const val TextOnRimBias = 0.82f
    val ResIds = intArrayOf(
        R.drawable.red_tile_1, R.drawable.red_tile_2, R.drawable.red_tile_3,
        R.drawable.red_tile_4, R.drawable.red_tile_5,
        R.drawable.red_tile_7, R.drawable.red_tile_8, R.drawable.red_tile_9,
        R.drawable.red_tile_10
    )
}
private object Ice { // NOTES (blue)
    const val Scale = 1.0f         // 100% zoom
    const val GlowStrength = 0.45f
    const val TextOnRimBias = 0.80f
    val ResIds = intArrayOf(
        R.drawable.blue_tile_1, R.drawable.blue_tile_2, R.drawable.blue_tile_3,
        R.drawable.blue_tile_4, R.drawable.blue_tile_5, R.drawable.blue_tile_6,
        R.drawable.blue_tile_7, R.drawable.blue_tile_8, R.drawable.blue_tile_9,
        R.drawable.blue_tile_10
    )
}
private object Rock { // CALENDAR (gray)
    const val Scale = 1.0f         // 100% zoom
    const val GlowStrength = 0.35f
    const val TextOnRimBias = 0.80f
    val ResIds = intArrayOf(
        R.drawable.gray_tile_1, R.drawable.gray_tile_2, R.drawable.gray_tile_3,
        R.drawable.gray_tile_5, R.drawable.gray_tile_6,
        R.drawable.gray_tile_7, R.drawable.gray_tile_8, R.drawable.gray_tile_9,
        R.drawable.gray_tile_10
    )
}
private object Moss { // WEEKLY GOALS (green)
    const val Scale = 1.1f         // ≈110% zoom
    const val GlowStrength = 0.40f
    const val TextOnRimBias = 0.82f
    val ResIds = intArrayOf(
        R.drawable.green_tile_1, R.drawable.green_tile_2, R.drawable.green_tile_3,
        R.drawable.green_tile_4, R.drawable.green_tile_5, R.drawable.green_tile_6,
        R.drawable.green_tile_7, R.drawable.green_tile_8, R.drawable.green_tile_9,
        R.drawable.green_tile_10
    )
}

enum class MedallionSegment { WEEKLY_GOALS, CALENDAR, EVENTS, NOTES }

private data class SegmentDefinition(
    val segment: MedallionSegment,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val color: Color
)

private data class EventSummary(
    val todayCount: Int = 0,
    val weekCount: Int = 0
)

private data class WeeklyGoalSliceSummary(
    val completed: Int = 0,
    val total: Int = 0
) {
    val cappedCompleted: Int = completed.coerceAtMost(total)
    val percent: Int = if (total <= 0) 0 else (cappedCompleted * 100) / total
}

private fun summarizeEvents(events: List<EventEntity>): EventSummary {
    val today = LocalDate.now()
    val todayDay = today.dayOfWeek
    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    var todayCount = 0
    var weekCount = 0

    events.forEach { entity ->
        val rawDate = entity.date?.trim().orEmpty()
        if (rawDate.isEmpty()) return@forEach

        if (rawDate.startsWith("Every", ignoreCase = true)) {
            val normalized = rawDate.lowercase(Locale.ENGLISH)
            val matchingDays = DayOfWeek.values().filter { day ->
                val name = day.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase(Locale.ENGLISH)
                normalized.contains(name)
            }
            if (matchingDays.contains(todayDay)) {
                todayCount += 1
            }
            weekCount += matchingDays.size
        } else {
            val parsed = runCatching { LocalDate.parse(rawDate) }.getOrNull()
            if (parsed != null) {
                if (parsed == today) {
                    todayCount += 1
                }
                if (!parsed.isBefore(startOfWeek) && !parsed.isAfter(endOfWeek)) {
                    weekCount += 1
                }
            }
        }
    }
    return EventSummary(todayCount = todayCount, weekCount = weekCount)
}

/** Animated backdrop (soft blobs) */
@Composable
private fun AnimatedBackdrop(modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val infinite = rememberInfiniteTransition()

    val shiftA by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val shiftB by infinite.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
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
    val infinite = rememberInfiniteTransition()
    val shift by infinite.animateFloat(
        initialValue = 0f, targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = Motion.WordmarkShiftMs, easing = LinearEasing)
        )
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
                shadow = Shadow(color = c.primary.copy(alpha = 0.35f), offset = Offset(2f, 3f), blurRadius = 10f)
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

/** ONE-IMAGE center-crop "cover" renderer (no tiling, no seams).
 *  - Crops a centered square region from the bitmap; `Scale` > 1f = more zoom.
 *  - Draws it to the full circle rect (cover), then clips to the wedge.
 *  - Result: while rotating, you always see one continuous image per slice.
 */
private fun DrawScope.drawSingleImageSlice(
    bitmap: ImageBitmap,
    start: Float,
    sweep: Float,
    rect: Rect,           // full circle bounds
    scale: Float,         // zoom per slice
    glowColor: Color,
    glowStrength: Float,
    center: Offset,
    rPx: Float
) {
    val wedge = arcPath(rect, start, sweep)

    // Compute centered square crop in bitmap space
    val bmpW = bitmap.width
    val bmpH = bitmap.height
    val baseSide = min(bmpW, bmpH).toFloat()
    val cropSide = (baseSide / scale).coerceAtLeast(1f) // smaller side -> zoom in
    val srcLeft = ((bmpW - cropSide) / 2f).toInt()
    val srcTop  = ((bmpH - cropSide) / 2f).toInt()
    val srcSize = IntSize(cropSide.toInt(), cropSide.toInt())

    // Destination is the full circle rect, so the image "covers" the medallion
    val dstOffset = IntOffset(rect.left.toInt(), rect.top.toInt())
    val dstSize   = IntSize(rect.width.toInt(), rect.height.toInt())

    clipPath(wedge) {
        drawImage(
            image = bitmap,
            srcOffset = IntOffset(srcLeft, srcTop),
            srcSize = srcSize,
            dstOffset = dstOffset,
            dstSize = dstSize
        )

        // Additive inner glow for depth
        drawArc(
            brush = Brush.radialGradient(
                listOf(glowColor.copy(alpha = 0.18f * glowStrength), Color.Transparent),
                center = center, radius = rPx * 0.95f
            ),
            startAngle = start, sweepAngle = sweep, useCenter = true,
            topLeft = rect.topLeft, size = rect.size, blendMode = BlendMode.Plus
        )
    }

    // soft rim highlight
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

    val context = LocalContext.current
    val notesDef = defs.getValue(MedallionSegment.NOTES)
    val notesContentColor = contentColorFor(notesDef.color)
    val eventsDef = defs.getValue(MedallionSegment.EVENTS)
    val eventsContentColor = contentColorFor(eventsDef.color)
    val weeklyGoalsDef = defs.getValue(MedallionSegment.WEEKLY_GOALS)
    val weeklyGoalsContentColor = contentColorFor(weeklyGoalsDef.color)
    val appContext = remember(context) { context.applicationContext }
    val database = remember(appContext) { EventDatabase.getInstance(appContext) }
    val notesCountFlow = remember(database) { database.noteDao().observeCount() }
    val notesCount by notesCountFlow.collectAsState(initial = 0)
    val eventSummaryFlow = remember(database) {
        database.eventDao().observeEvents().map(::summarizeEvents)
    }
    val eventSummary by eventSummaryFlow.collectAsState(initial = EventSummary())
    val weeklyGoalsFlow = remember(database) { database.weeklyGoalDao().observeGoals() }
    val weeklyGoals by weeklyGoalsFlow.collectAsState(initial = emptyList())
    val weeklyGoalsSummary = remember(weeklyGoals) {
        var completed = 0
        var total = 0
        weeklyGoals.map { it.toModel() }.forEach { goal ->
            val frequency = goal.frequency.coerceAtLeast(0)
            val done = goal.dayStates.count { it == 'C' }.coerceAtMost(frequency)
            completed += done
            total += frequency
        }
        WeeklyGoalSliceSummary(completed = completed, total = total)
    }
    val calendarDef = defs.getValue(MedallionSegment.CALENDAR)
    val calendarContentColor = contentColorFor(calendarDef.color)
    val today = LocalDate.now()
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE")
    val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d")
    val calendarSubtitle = "${today.format(dayOfWeekFormatter)} • ${today.format(monthDayFormatter)}"

    var order by rememberSaveable {
        mutableStateOf(listOf(MedallionSegment.NOTES, MedallionSegment.WEEKLY_GOALS, MedallionSegment.EVENTS, MedallionSegment.CALENDAR))
    }

    // wheel rotation
    var angleDeg by rememberSaveable { mutableStateOf(0f) }
    var spinning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val outlineRing: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    // random variant per cold launch
    val lavaResId  by rememberSaveable { mutableIntStateOf(Lava.ResIds.random()) }
    val iceResId   by rememberSaveable { mutableIntStateOf(Ice.ResIds.random()) }
    val rockResId  by rememberSaveable { mutableIntStateOf(Rock.ResIds.random()) }
    val mossResId  by rememberSaveable { mutableIntStateOf(Moss.ResIds.random()) }

    val lavaBitmap = ImageBitmap.imageResource(lavaResId)
    val iceBitmap  = ImageBitmap.imageResource(iceResId)
    val rockBitmap = ImageBitmap.imageResource(rockResId)
    val mossBitmap = ImageBitmap.imageResource(mossResId)

    // No texture motion needed; keep simple constants (fixes your tween/easing error)
    val lavaPhase = 0f
    val icePhase = 0f
    val rockPhase = 0f
    val mossPhase = 0f
    @Suppress("UNUSED_VARIABLE")
    val _ignoreUseToAvoidWarnings = lavaPhase + icePhase + rockPhase + mossPhase

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

                        // outer ring
                        drawCircle(
                            color = outlineRing, radius = rPx, center = center,
                            style = Stroke(width = with(density) { 2.dp.toPx() })
                        )

                        // slices with ONE centered image each (no tiling)
                        repeat(4) { i ->
                            val seg = order[i]
                            val start = mids[i] + angleDeg - 45f - Wheel.ArcEpsilonDeg
                            val sweep = 90f + Wheel.ArcEpsilonDeg * 2f

                            when (seg) {
                                MedallionSegment.EVENTS -> drawSingleImageSlice(
                                    bitmap = lavaBitmap,
                                    start = start, sweep = sweep, rect = rect,
                                    scale = Lava.Scale,
                                    glowColor = Color(0xFFFF7A00), glowStrength = Lava.GlowStrength,
                                    center = center, rPx = rPx
                                )
                                MedallionSegment.NOTES -> drawSingleImageSlice(
                                    bitmap = iceBitmap,
                                    start = start, sweep = sweep, rect = rect,
                                    scale = Ice.Scale,
                                    glowColor = Color(0xFF6AD0FF), glowStrength = Ice.GlowStrength,
                                    center = center, rPx = rPx
                                )
                                MedallionSegment.CALENDAR -> drawSingleImageSlice(
                                    bitmap = rockBitmap,
                                    start = start, sweep = sweep, rect = rect,
                                    scale = Rock.Scale,
                                    glowColor = Color(0xFF7EC8A6), glowStrength = Rock.GlowStrength,
                                    center = center, rPx = rPx
                                )
                                MedallionSegment.WEEKLY_GOALS -> drawSingleImageSlice(
                                    bitmap = mossBitmap,
                                    start = start, sweep = sweep, rect = rect,
                                    scale = Moss.Scale,
                                    glowColor = Color(0xFF7CFF63), glowStrength = Moss.GlowStrength,
                                    center = center, rPx = rPx
                                )
                            }
                        }

                        // faint rotating light
                        rotate(angleDeg) {
                            drawCircle(
                                brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)),
                                radius = rPx * 0.7f, center = Offset(center.x, center.y - rPx * 0.15f)
                            )
                        }
                    }

                    // Tap detection overlay & labels
                    Box(Modifier.size(diameterDp)) {
                        val goalsIndex = order.indexOf(MedallionSegment.WEEKLY_GOALS)
                        val eventsIndex = order.indexOf(MedallionSegment.EVENTS)
                        val calendarIndex = order.indexOf(MedallionSegment.CALENDAR)
                        val notesIndex = order.indexOf(MedallionSegment.NOTES)
                        val radiusPx = with(density) { radiusDp.toPx() }
                        val labelRadiusPx = radiusPx * 0.58f

                        if (goalsIndex >= 0) {
                            val midAngle = mids[goalsIndex] + angleDeg
                            val angleRad = Math.toRadians(midAngle.toDouble())
                            val offsetX = (cos(angleRad) * labelRadiusPx).roundToInt()
                            val offsetY = (sin(angleRad) * labelRadiusPx).roundToInt()

                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset { IntOffset(offsetX, offsetY) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = stringResource(defs.getValue(MedallionSegment.WEEKLY_GOALS).labelRes),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = weeklyGoalsContentColor.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "${weeklyGoalsSummary.percent}%",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = weeklyGoalsContentColor.copy(alpha = 0.95f)
                                )
                                Text(
                                    text = "This week: ${weeklyGoalsSummary.cappedCompleted}/${weeklyGoalsSummary.total}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = weeklyGoalsContentColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                        if (eventsIndex >= 0) {
                            val midAngle = mids[eventsIndex] + angleDeg
                            val angleRad = Math.toRadians(midAngle.toDouble())
                            val offsetX = (cos(angleRad) * labelRadiusPx).roundToInt()
                            val offsetY = (sin(angleRad) * labelRadiusPx).roundToInt()

                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset { IntOffset(offsetX, offsetY) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = stringResource(defs.getValue(MedallionSegment.EVENTS).labelRes),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = eventsContentColor.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "Today: ${eventSummary.todayCount}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                    color = eventsContentColor.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "This week: ${eventSummary.weekCount}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                    color = eventsContentColor.copy(alpha = 0.9f)
                                )
                            }
                        }
                        if (calendarIndex >= 0) {
                            val midAngle = mids[calendarIndex] + angleDeg
                            val angleRad = Math.toRadians(midAngle.toDouble())
                            val offsetX = (cos(angleRad) * labelRadiusPx).roundToInt()
                            val offsetY = (sin(angleRad) * labelRadiusPx).roundToInt()

                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset { IntOffset(offsetX, offsetY) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = stringResource(defs.getValue(MedallionSegment.CALENDAR).labelRes),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = calendarContentColor.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = calendarSubtitle,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                    color = calendarContentColor.copy(alpha = 0.9f)
                                )
                            }
                        }
                        if (notesIndex >= 0) {
                            val midAngle = mids[notesIndex] + angleDeg
                            val angleRad = Math.toRadians(midAngle.toDouble())
                            val offsetX = (cos(angleRad) * labelRadiusPx).roundToInt()
                            val offsetY = (sin(angleRad) * labelRadiusPx).roundToInt()

                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset { IntOffset(offsetX, offsetY) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = stringResource(defs.getValue(MedallionSegment.NOTES).labelRes),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = eventsContentColor.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "Total: $notesCount",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                    color = eventsContentColor.copy(alpha = 0.9f)
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
