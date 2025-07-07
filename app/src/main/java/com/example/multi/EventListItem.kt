package com.example.multi

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable

/**
 * Displays a single event with modern styling.
 */
@Composable
fun EventListItem(
    event: Event,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier.then(clickMod)
    ) {
        ListItem(
            leadingContent = {
                Icon(Icons.Default.Event, contentDescription = null)
            },
            headlineContent = { Text(event.title) },
            supportingContent = {
                if (event.description.isNotBlank()) {
                    Text(event.description)
                }
            },
            trailingContent = {
                event.date?.let {
                    AssistChip(
                        onClick = {},
                        colors = AssistChipDefaults.assistChipColors(),
                        label = { Text(it) }
                    )
                }
            }
        )
    }
}

