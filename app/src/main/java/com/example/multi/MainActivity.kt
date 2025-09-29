package com.example.multi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.Scaffold
import androidx.compose.ui.Modifier
import com.example.multi.ui.theme.MultiTheme
import androidx.lifecycle.lifecycleScope
import com.example.multi.data.EventDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.multi.util.LastVisitedPreferences

/**
 * Main entry point of the application.
 *
 * Hosts the [MedallionScreen] which provides navigation to the various
 * feature segments.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val db = EventDatabase.getInstance(this@MainActivity)
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            withContext(Dispatchers.IO) {
                db.trashedNoteDao().deleteExpired(threshold)
                db.trashedEventDao().deleteExpired(threshold)
            }
        }
        scheduleDailyActivityReminder(this)
        val skipLastVisited = intent?.getBooleanExtra(EXTRA_SKIP_LAST_VISITED, false) ?: false
        if (skipLastVisited) {
            intent?.removeExtra(EXTRA_SKIP_LAST_VISITED)
        }
        if (!skipLastVisited && savedInstanceState == null) {
            val target = LastVisitedPreferences.getLastVisitedActivity(this)
                ?: NotesActivity::class.java
            if (target != this::class.java) {
                startActivity(Intent(this, target))
            }
        }
        enableEdgeToEdge()
        setContent {
            MultiTheme(darkTheme = ThemePreferences.isDarkTheme(this)) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MedallionScreen()
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_SKIP_LAST_VISITED = "extra_skip_last_visited"
    }
}
