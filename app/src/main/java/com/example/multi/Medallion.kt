package com.example.multi

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign

/** Enum describing each clickable segment of the medallion. */
enum class MedallionSegment { STONE, IRON, WOOD, MAGMA }

/**
 * Draws a circular medallion divided in four clickable quadrants representing
 * stone, iron, wood and magma.
 */
@Composable
fun Medallion(
    modifier: Modifier = Modifier,
    onSegmentClick: (MedallionSegment) -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(350.dp)
            .clip(CircleShape)
            .border(4.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFE0E0E0), Color(0xFF757575))
                            )
                        )
)
                        .drawBehind {
                            val stroke = 1.5.dp.toPx()
                            drawLine(Color.DarkGray, Offset(size.width * 0.1f, size.height * 0.3f), Offset(size.width * 0.9f, size.height * 0.35f), strokeWidth = stroke)
                            drawLine(Color.DarkGray, Offset(size.width * 0.2f, size.height * 0.7f), Offset(size.width * 0.8f, size.height * 0.85f), strokeWidth = stroke)
                            drawLine(Color.DarkGray, Offset(size.width * 0.55f, 0f), Offset(size.width * 0.45f, size.height), strokeWidth = stroke)
                            drawLine(Color.DarkGray, Offset(size.width * 0.3f, size.height * 0.45f), Offset(size.width * 0.8f, size.height * 0.55f), strokeWidth = stroke)
                        }
                        .clickable { onSegmentClick(MedallionSegment.STONE) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Calendar",
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFECEFF1),
                                    Color(0xFFB0BEC5),
                                    Color(0xFFECEFF1)
                                ),
                                start = Offset.Zero,
                                end = Offset.Infinite
                            )
)
                        .drawBehind {
                            val highlight = Brush.verticalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                                startY = 0f,
                                endY = size.height * 0.4f
                            )
                            drawRect(highlight)
                        }
                        .clickable { onSegmentClick(MedallionSegment.IRON) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Events",
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFA1887F), Color(0xFF4E342E))
                            )
                        )
                        .drawBehind {
                            val stroke = 1.dp.toPx()
                            val step = size.width / 6
                            for (i in 1..5) {
                                val x = step * i
                                drawLine(
                                    Color(0xFF3E2723),
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = stroke
                                )
                            }
                        } // wood with splinters
                        .clickable { onSegmentClick(MedallionSegment.WOOD) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Workout",
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .drawBehind {
                            val infinite = rememberInfiniteTransition()
                            val outerProgress by infinite.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(1000), repeatMode = RepeatMode.Reverse)
                            )
                            val innerProgress by infinite.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(1000), repeatMode = RepeatMode.Reverse)
                            )
                            val outer = lerp(Color(0xFFFFA726), Color(0xFFFF7043), outerProgress)
                            val inner = lerp(Color(0xFFFF7043), Color(0xFFD84315), innerProgress)
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(outer, inner),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.minDimension * 0.8f
                                )
                            )
                        }
                        .clickable { onSegmentClick(MedallionSegment.MAGMA) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Notes",
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
/** Simple screen displaying the [Medallion] in the center. */
@Composable
fun MedallionScreen() {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Medallion { segment ->
            val cls = when (segment) {
                MedallionSegment.STONE -> CalendarActivity::class.java
                MedallionSegment.IRON -> EventsActivity::class.java
                MedallionSegment.WOOD -> WorkoutActivity::class.java
                MedallionSegment.MAGMA -> NotesActivity::class.java
            }
            context.startActivity(Intent(context, cls))
        }
    }
}
