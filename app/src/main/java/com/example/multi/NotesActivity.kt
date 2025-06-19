package com.example.multi

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class NotesActivity : SegmentActivity(
    "Notes",
    showBackButton = false,
    showCloseButton = false
) {
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text("Notes", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("This is where your notes will appear.")
            }

            ExtendedFloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, NoteEditorActivity::class.java))
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { M3Text("New Note") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp)
            )
        }
    }
}
