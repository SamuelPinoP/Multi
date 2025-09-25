package com.example.multi

import android.content.Intent
import androidx.activity.ComponentActivity

/**
 * Activity that redirects the user to [MainActivity] when there is no
 * previous activity in the back stack. This prevents the app from closing
 * when opened from a notification or any entry point without history.
 */
open class BaseActivity : ComponentActivity() {
    /** Indicates whether this activity should be restored as the last visited. */
    protected open fun shouldRememberAsLastVisited(): Boolean = true

    /**
     * Handles navigation when the user presses back. If this activity is the
     * root of the task, [MainActivity] is launched instead of closing.
     */
    protected fun navigateBackOrFinish() {
        if (isTaskRoot) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }

    override fun onBackPressed() {
        navigateBackOrFinish()
    }

    override fun onResume() {
        super.onResume()
        if (shouldRememberAsLastVisited()) {
            LastVisitedActivityPreferences.remember(this)
        }
    }
}
