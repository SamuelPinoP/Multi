package com.example.multi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun GradientDangerButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    gradient: Brush,
    borderBrush: Brush,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .shadow(
                if (enabled) 12.dp else 0.dp,
                shape = shape,
                clip = false,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            )
            .background(
                if (enabled) gradient
                else Brush.linearGradient(listOf(Color(0xFF616161), Color(0xFF4A4A4A))),
                shape
            )
            .border(
                1.dp,
                if (enabled) borderBrush
                else Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))),
                shape
            )
            .clip(shape)
    ) {
        Icon(
            imageVector = Icons.Filled.DeleteForever,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun PrettyConfirmDialog(
    visible: Boolean,
    title: String,
    itemName: String,
    count: Int,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    Dialog(onDismissRequest = onCancel) {
        val shape = RoundedCornerShape(24.dp)
        val border = Brush.linearGradient(
            listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
        )
        val p = MaterialTheme.colorScheme.primary
        val headerGrad = Brush.horizontalGradient(
            listOf(lerp(p, Color.White, 0.12f), lerp(p, Color.Black, 0.18f))
        )

        Surface(
            shape = shape,
            tonalElevation = 4.dp,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, shape)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(headerGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            "This will permanently delete $count ${if (count == 1) itemName else "${itemName}s"}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

