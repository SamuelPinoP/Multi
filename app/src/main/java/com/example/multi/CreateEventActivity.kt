package com.example.multi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.ui.theme.MultiTheme

/**
 * Activity that allows the user to create a new calendar event.
 */
class CreateEventActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                CreateEventScreen { finishAfterSave() }
            }
        }
    }

    /**
     * Displays a brief confirmation and closes the screen after saving.
     */
    private fun finishAfterSave() {
        Toast.makeText(this, "Event saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}

@Composable
/**
 * Form used to capture a new event from the user.
 */
private fun CreateEventScreen(onSave: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    androidx.compose.material.Scaffold(
    topBar = {
        TopAppBar(
            modifier = Modifier.height(135.dp),
            title = {
                Text(
                    text = "New Event",
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp)
                )
            }
        )
    },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (title.isNotBlank()) onSave() },
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
