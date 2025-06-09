package com.example.multi

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
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
                        ) // stone
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
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFCFD8DC), Color(0xFF455A64))
                            )
                        ) // iron
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
                        ) // wood (rock)
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
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFD54F),
                                    Color(0xFFFF7043),
                                    Color(0xFFD84315)
                                )
                            )
                        ) // magma
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
        // Overlay cross lines for a cleaner look
        Box(
            Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .height(2.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
        )
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
