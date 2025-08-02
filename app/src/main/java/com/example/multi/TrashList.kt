package com.example.multi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable trash list composable used by note and event trash screens.
 */
@Composable
fun <T> TrashList(
    items: List<T>,
    deletedTime: (T) -> Long,
    cardModifier: (T) -> Modifier = { Modifier },
    onRestore: (T) -> Unit,
    onDelete: (T) -> Unit,
    itemContent: @Composable ColumnScope.(T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Trash is empty",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    val daysLeft = ((deletedTime(item) + 30L * 24 * 60 * 60 * 1000 - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
                    ElevatedCard(
                        elevation = CardDefaults.elevatedCardElevation(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = cardModifier(item)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            itemContent(item)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Days remaining: $daysLeft",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onRestore(item) }) { Text("Restore") }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { onDelete(item) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}
