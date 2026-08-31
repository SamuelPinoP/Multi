package com.example.multi

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.example.multi.ui.theme.MultiTheme

open class SegmentActivity(
    private val segmentTitle: String,
) : BaseActivity() {
    @Composable
    open fun SegmentContent() {
        Text(segmentTitle)
    }

    @Composable
    open fun SegmentActions() {}

    @Composable
    open fun OverflowMenuItems(onDismiss: () -> Unit) {}

    /** Screens that provide [OverflowMenuItems] set this so the ⋮ button appears. */
    open val hasOverflowMenu: Boolean get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkThemeState = remember { mutableStateOf(ThemePreferences.isDarkTheme(this)) }
            MultiTheme(darkTheme = darkThemeState.value) {
                SegmentScreen(
                    title = segmentTitle,
                    onBack = { navigateBackOrFinish() },
                    actions = {
                        SegmentActions()
                        OverflowAction(
                            darkThemeState = darkThemeState,
                            hasOverflowMenu = hasOverflowMenu,
                        ) { OverflowMenuItems(it) }
                    },
                ) {
                    SegmentContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentScreen(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = actions,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.background,
                        1f to MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                ),
        ) {
            content()
        }
    }
}

@Composable
private fun OverflowAction(
    darkThemeState: MutableState<Boolean>,
    hasOverflowMenu: Boolean,
    extraItems: @Composable (onDismiss: () -> Unit) -> Unit = {},
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val isDark = darkThemeState.value

    IconButton(onClick = {
        val newValue = !isDark
        darkThemeState.value = newValue
        ThemePreferences.setDarkTheme(context, newValue)
    }) {
        Icon(
            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDark) "Switch to light theme" else "Switch to dark theme",
        )
    }

    if (hasOverflowMenu) {
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                extraItems { expanded = false }
            }
        }
    }
}
