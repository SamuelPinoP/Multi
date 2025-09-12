package com.example.multi

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private data class SegmentInfo(
    val segment: MedallionSegment,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun PieMedallion(
    modifier: Modifier = Modifier,
    onSegmentClick: (MedallionSegment) -> Unit = {}
) {
    val c = MaterialTheme.colorScheme
    val defs = remember(c) {
        listOf(
            SegmentInfo(MedallionSegment.NOTES, Icons.Default.Note, c.inversePrimary),
            SegmentInfo(MedallionSegment.WEEKLY_GOALS, Icons.Default.Flag, c.primaryContainer),
            SegmentInfo(MedallionSegment.EVENTS, Icons.Default.Event, c.tertiaryContainer),
            SegmentInfo(MedallionSegment.CALENDAR, Icons.Default.DateRange, c.secondaryContainer)
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f
                    val dx = offset.x - cx
                    val dy = offset.y - cy
                    val r = min(cx, cy)
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist <= r) {
                        var ang = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                        ang = (ang + 360 + 90) % 360
                        val index = (ang / 90).toInt()
                        onSegmentClick(defs[index].segment)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.matchParentSize()) {
            val sweep = 90f
            var start = -90f
            for (info in defs) {
                drawArc(
                    color = info.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = true
                )
                start += sweep
            }
        }

        val iconSize = 32.dp
        val r: Dp = maxWidth / 2
        val iconDist = r * 0.6f
        defs.forEachIndexed { index, info ->
            val angleRad = Math.toRadians((index * 90 + 45).toDouble())
            val x = r + iconDist * cos(angleRad).toFloat()
            val y = r + iconDist * sin(angleRad).toFloat()
            Icon(
                imageVector = info.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .offset(x - iconSize / 2, y - iconSize / 2)
            )
        }
    }
}

