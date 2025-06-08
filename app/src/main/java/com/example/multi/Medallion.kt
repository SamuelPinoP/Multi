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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

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
            .size(250.dp)
            .clip(CircleShape)
            .border(4.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFF9E9E9E)) // stone
                        .clickable { onSegmentClick(MedallionSegment.STONE) }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFFB0BEC5)) // iron
                        .clickable { onSegmentClick(MedallionSegment.IRON) }
                )
            }
            Row(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFF8D6E63)) // wood
                        .clickable { onSegmentClick(MedallionSegment.WOOD) }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFFFF7043)) // magma
                        .clickable { onSegmentClick(MedallionSegment.MAGMA) }
                )
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
