package com.example.multi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable row of seven day buttons. When [onClick] is null the buttons are
 * displayed read-only.
 */
@Composable
fun DayButtonsRow(
    states: String,
    onClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val labels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        labels.forEachIndexed { index, label ->
            val color = when (states[index]) {
                'C' -> Color(0xFF4CAF50)
                'M' -> Color.Red
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Button(
                onClick = { onClick?.invoke(index) },
                enabled = onClick != null,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Text(label)
            }
        }
    }
}
