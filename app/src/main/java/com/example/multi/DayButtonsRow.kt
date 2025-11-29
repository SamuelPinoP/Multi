package com.example.multi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun DayButtonsRow(states: String, onClick: (Int) -> Unit) {
    val labels = listOf("S", "M", "T", "W", "T", "F", "S")
    val borderBrushes = listOf(
        Brush.linearGradient(listOf(Color(0xFF6A11CB), Color(0xFF2575FC))),
        Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
        Brush.linearGradient(listOf(Color(0xFFFF9966), Color(0xFFFF5E62))),
        Brush.linearGradient(listOf(Color(0xFFFFC371), Color(0xFFFF5F6D))),
        Brush.linearGradient(listOf(Color(0xFF36D1DC), Color(0xFF5B86E5))),
        Brush.linearGradient(listOf(Color(0xFFF2994A), Color(0xFFF2C94C))),
        Brush.linearGradient(listOf(Color(0xFFABDCFF), Color(0xFF0396FF)))
    )
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        labels.forEachIndexed { index, label ->
            val color = when (states[index]) {
                'C' -> Color(0xFF4CAF50)
                'M' -> Color.Red
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val contentColor = if (color.luminance() < 0.45f) Color.White else MaterialTheme.colorScheme.onSurface
            Button(
                onClick = { onClick(index) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = color,
                    contentColor = contentColor
                ),
                border = BorderStroke(1.5.dp, borderBrushes[index % borderBrushes.size]),
                shape = shape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(44.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 4.dp)
            ) {
                Text(label)
            }
        }
    }
}
