package com.example.multi

import android.content.Intent
import androidx.activity.ComponentActivity
import com.example.multi.util.LastVisitedPreferences

/**
 * Activity that redirects the user to [MainActivity] when there is no
 * previous activity in the back stack. This prevents the app from closing
 * when opened from a notification or any entry point without history.
 */
open class BaseActivity : ComponentActivity() {
    /**
     * Handles navigation when the user presses back. If this activity is the
     * root of the task, [MainActivity] is launched instead of closing.
     */
    protected fun navigateBackOrFinish() {
        if (isTaskRoot) {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(MainActivity.EXTRA_SKIP_LAST_VISITED, true)
                }
            )
        }
        finish()
    }

    override fun onBackPressed() {
        navigateBackOrFinish()
    }

    override fun onResume() {
        super.onResume()
        LastVisitedPreferences.setLastVisitedActivity(this, this::class.java)
    }
}
