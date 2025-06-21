package com.example.multi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class WorkoutActivity : SegmentActivity("Workout") {
    @Composable
    override fun SegmentContent() {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Today's workout", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("\u2022 Push-ups\n\u2022 Sit-ups\n\u2022 Squats")
        }
    }
}
