package com.example.multi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import com.example.multi.ui.components.GradientDangerButton
import com.example.multi.ui.components.PrettyConfirmDialog
import com.example.multi.util.openAddressInMaps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Activity showing deleted events. */
class EventTrashActivity : SegmentActivity("Trash") {
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val events = remember { mutableStateListOf<TrashedEvent>() }
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        var showConfirm by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val dao = EventDatabase.getInstance(context).trashedEventDao()
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            withContext(Dispatchers.IO) { dao.deleteExpired(threshold) }
            val stored = withContext(Dispatchers.IO) { dao.getEvents() }
            events.clear(); events.addAll(stored.map { it.toModel() })
        }

        val primaryTint = MaterialTheme.colorScheme.primary
        val brandGradient = remember(primaryTint) {
            Brush.horizontalGradient(
                listOf(
                    lerp(primaryTint, Color.White, 0.12f),
                    lerp(primaryTint, Color.Black, 0.18f)
                )
            )
        }
        val brandBorder = Brush.horizontalGradient(
            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.18f))
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GradientDangerButton(
                    text = "Clear trash",
                    enabled = events.isNotEmpty(),
                    gradient = brandGradient,
                    borderBrush = brandBorder,
                    onClick = { showConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                TrashList(
                    items = events,
                    deletedTime = { it.deleted },
                    cardModifier = { Modifier.fillMaxWidth() },
                    onRestore = { event ->
                        scope.launch {
                            val db = EventDatabase.getInstance(context)
                            withContext(Dispatchers.IO) {
                                db.eventDao().insert(
                                    Event(
                                        title = event.title,
                                        description = event.description,
                                        date = event.date,
                                        address = event.address
                                    ).toEntity()
                                )
                                db.trashedEventDao().delete(event.toEntity())
                            }
                            events.remove(event)
                            snackbarHostState.showSnackbar("Event restored.")
                        }
                    },
                    onDelete = { event ->
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).trashedEventDao()
                            withContext(Dispatchers.IO) { dao.delete(event.toEntity()) }
                            events.remove(event)
                            snackbarHostState.showSnackbar("Deleted forever.")
                        }
                    }
                ) { event ->
                    Text(event.title, style = MaterialTheme.typography.titleMedium)
                    if (event.description.isNotBlank()) {
                        Text(event.description)
                    }
                    event.date?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall)
                    }
                    event.address?.takeIf { it.isNotBlank() }?.let { addr ->
                        Text(
                            addr,
                            color = Color.Blue,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { openAddressInMaps(context, addr) }
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            )

            PrettyConfirmDialog(
                visible = showConfirm,
                title = "Clear event trash?",
                itemName = "event",
                count = events.size,
                onCancel = { showConfirm = false },
                onConfirm = {
                    showConfirm = false
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).trashedEventDao()
                        withContext(Dispatchers.IO) { dao.deleteAll() }
                        events.clear()
                        snackbarHostState.showSnackbar("Trash cleared.")
                    }
                }
            )
        }
    }
}

