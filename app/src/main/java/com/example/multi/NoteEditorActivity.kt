package com.example.multi

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteEditorActivity : SegmentActivity("New Note") {
    @Composable
    override fun SegmentContent() {
        Surface(modifier = Modifier.fillMaxSize()) {
            val textState = remember { mutableStateOf("") }
            val context = LocalContext.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (textState.value.isEmpty()) {
                    Text(
                        text = "Start writing...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BasicTextField(
                    value = textState.value,
                    onValueChange = { textState.value = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
                )

                ExtendedFloatingActionButton(
                    onClick = {
                        val activity = context as Activity
                        if (textState.value.isNotBlank()) {
                            activity.lifecycleScope.launch {
                                val dao = EventDatabase.getInstance(activity).noteDao()
                                withContext(Dispatchers.IO) {
                                    dao.insert(Note(content = textState.value).toEntity())
                                }
                                activity.finish()
                            }
                        } else {
                            activity.finish()
                        }
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Save") },
                    text = { Text("Save") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}
