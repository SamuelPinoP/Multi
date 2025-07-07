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
import androidx.compose.ui.res.stringResource

class WorkoutActivity : SegmentActivity(R.string.label_workout) {
    @Composable
    override fun SegmentContent() {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.todays_workout), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.workout_items))
        }
    }
}
