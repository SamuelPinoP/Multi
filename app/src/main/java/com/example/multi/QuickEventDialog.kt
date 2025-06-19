package com.example.multi

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity

/**
 * Minimal dialog used by [CalendarActivity] to quickly create an event with
 * the selected date. The event is saved with empty title and description.
 */
@Composable
fun QuickEventDialog(
    date: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    val dao = EventDatabase.getInstance(context).eventDao()
                    withContext(Dispatchers.IO) {
                        dao.insert(Event(title = "", description = "", date = date).toEntity())
                    }
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { Text("Create event on $date?") }
    )
}
