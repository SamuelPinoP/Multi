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

/**
 * Main entry point of the application.
 *
 * Hosts the [MedallionScreen] which provides navigation to the various
 * feature segments.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If this is a fresh launch and a last screen was stored, start that
        if (savedInstanceState == null) {
            val last = com.example.multi.util.LastActivityTracker.getLastActivityClass(this)
            if (last != null && last != MainActivity::class.java) {
                startActivity(Intent(this, last))
                finish()
                return
            }
        }

        enableEdgeToEdge()
        setContent {
            MultiTheme {
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

    override fun onResume() {
        super.onResume()
        // Record MainActivity as the last opened when it's visible
        com.example.multi.util.LastActivityTracker.saveLastActivity(this, MainActivity::class.java)
    }
}
