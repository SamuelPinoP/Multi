package com.example.multi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.multi.ui.theme.MultiTheme
import com.example.multi.ThemePreferences
import androidx.lifecycle.lifecycleScope
import com.example.multi.data.EventDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
        enableEdgeToEdge()
        setContent {
            MultiTheme(darkTheme = ThemePreferences.isDarkTheme(this)) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "medallion",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("medallion") { MedallionScreen(navController) }
                    composable("calendar") { CalendarMenuScreen() }
                    composable("events") { EventsRoute(navController) }
                    composable("weekly_goals") { WeeklyGoalsRoute(navController) }
                    composable("notes") { NotesRoute(navController) }
                }
            }
        }
    }
}
