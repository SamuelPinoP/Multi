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
import androidx.compose.ui.unit.dp

@Composable
fun DayButtonsRow(states: String, onClick: (Int) -> Unit) {
    val labels = listOf("S", "M", "T", "W", "T", "F", "S")
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

            val borderBrush = when (states[index]) {
                'C' -> Brush.linearGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32)))
                'M' -> Brush.linearGradient(listOf(Color(0xFFFF8A65), Color(0xFFD32F2F)))
                else -> Brush.linearGradient(listOf(Color(0xFFB0BEC5), Color(0xFF90A4AE)))
            }
            Button(
                onClick = { onClick(index) },
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, borderBrush),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Text(label)
            }
        }
    }
}
