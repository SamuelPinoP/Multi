package com.example.multi

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class NoteEditorActivity : SegmentActivity("New Note") {
    @Composable
    override fun SegmentContent() {
        NoteEditorScreen { finish() }
    }
}

@Composable
private fun NoteEditorScreen(onDone: () -> Unit) {
    val textState = remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onDone,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Done")
            }
        }
    ) { padding ->
        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = { Text("Start typing...") }
        )
    }
}
