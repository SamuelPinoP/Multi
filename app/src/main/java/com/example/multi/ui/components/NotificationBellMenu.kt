package com.example.multi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NotificationBellMenu(
    hasNotification: Boolean,
    onAddNotification: () -> Unit,
    onRemoveNotification: () -> Unit,
    onEditNotification: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.wrapContentSize(Alignment.TopEnd)) {
        Icon(
            imageVector = if (hasNotification) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
            contentDescription = if (hasNotification) {
                "Notification enabled"
            } else {
                "Notification disabled"
            },
            tint = if (hasNotification) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier
                .size(iconSize)
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Add notification") },
                enabled = !hasNotification,
                onClick = {
                    expanded = false
                    onAddNotification()
                }
            )
            DropdownMenuItem(
                text = { Text("Remove notification") },
                enabled = hasNotification,
                onClick = {
                    expanded = false
                    onRemoveNotification()
                }
            )
            DropdownMenuItem(
                text = { Text("Edit notification time") },
                enabled = hasNotification,
                onClick = {
                    expanded = false
                    onEditNotification()
                }
            )
        }
    }
}
