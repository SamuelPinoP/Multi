package com.example.multi

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** Activity showing deleted notes. */
class TrashbinActivity : SegmentActivity("Trash Bin") {
    @Composable
    override fun SegmentContent() {
        Text("Trashbin is empty")
    }
}
