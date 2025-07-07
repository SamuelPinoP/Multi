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
import com.example.multi.util.LaunchPrefs

/**
 * Main entry point of the application.
 *
 * Hosts the [MedallionScreen] which provides navigation to the various
 * feature segments.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && intent?.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
            LaunchPrefs.getLastActivityClass(this)?.let { cls ->
                if (cls != MainActivity::class.java) {
                    startActivity(Intent(this, cls))
                    finish()
                    return
                }
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
        LaunchPrefs.setLastActivity(this)
    }
}
