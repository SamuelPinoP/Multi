package com.example.multi

import android.widget.Toast
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
            .size(320.dp)
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
                                listOf(Color(0xFFBDBDBD), Color(0xFF757575))
                            )
                        ) // stone
                        .clickable { onSegmentClick(MedallionSegment.STONE) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Calendar", color = MaterialTheme.colorScheme.onBackground)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFCFD8DC), Color(0xFF607D8B))
                            )
                        ) // iron
                        .clickable { onSegmentClick(MedallionSegment.IRON) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Events", color = MaterialTheme.colorScheme.onBackground)
                }
            }
            Row(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFA1887F), Color(0xFF5D4037))
                            )
                        ) // wood (rock)
                        .clickable { onSegmentClick(MedallionSegment.WOOD) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Workout", color = MaterialTheme.colorScheme.onBackground)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFFFAB40), Color(0xFFD84315))
                            )
                        ) // magma
                        .clickable { onSegmentClick(MedallionSegment.MAGMA) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Notes", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        // Overlay cross lines for a cleaner look
        Box(
            Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .height(2.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        )
    }
}

/** Simple screen displaying the [Medallion] in the center. */
@Composable
fun MedallionScreen() {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Medallion { segment ->
            Toast.makeText(context, "Clicked $segment", Toast.LENGTH_SHORT).show()
        }
    }
}
