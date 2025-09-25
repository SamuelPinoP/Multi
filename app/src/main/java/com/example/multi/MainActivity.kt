package com.example.multi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.multi.ui.theme.MultiTheme
import com.example.multi.ThemePreferences
import androidx.lifecycle.lifecycleScope
import com.example.multi.data.EventDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.multi.navigation.AppStartDestinationResolver
import com.example.multi.navigation.MultiApp

/**
 * Main entry point of the application.
 *
 * Hosts the Jetpack Navigation graph that renders the onboarding, lock and
 * feature screens using Compose.
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
        enableEdgeToEdge()
        val startDestination = AppStartDestinationResolver(this).resolve()
        setContent {
            MultiTheme(darkTheme = ThemePreferences.isDarkTheme(this)) {
                MultiApp(startDestination = startDestination)
            }
        }
    }
}
