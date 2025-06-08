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
            .size(200.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFF8B8B8B)) // stone
                        .clickable { onSegmentClick(MedallionSegment.STONE) }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFFB0B0B0)) // iron
                        .clickable { onSegmentClick(MedallionSegment.IRON) }
                )
            }
            Row(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFF8B4513)) // wood
                        .clickable { onSegmentClick(MedallionSegment.WOOD) }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFFFF4500)) // magma
                        .clickable { onSegmentClick(MedallionSegment.MAGMA) }
                )
            }
        }
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
