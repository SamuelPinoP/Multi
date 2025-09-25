package com.example.multi

import android.content.Intent
import androidx.activity.ComponentActivity

/**
 * Activity that redirects the user to a configurable landing screen when
 * there is no previous activity in the back stack. This prevents the app from
 * closing when opened from a notification or any entry point without
 * history.
 */
open class BaseActivity : ComponentActivity() {
    /**
     * Returns the activity that should be launched when this activity is the
     * root of the task and the user navigates back. Subclasses can override
     * this to provide a different landing activity.
     */
    protected open fun rootActivityClass(): Class<out ComponentActivity> = MainActivity::class.java

    /**
     * Handles navigation when the user presses back. If this activity is the
     * root of the task, [rootActivityClass] is launched instead of closing.
     */
    protected fun navigateBackOrFinish() {
        if (isTaskRoot) {
            startActivity(Intent(this, rootActivityClass()))
        }
        finish()
    }

    override fun onBackPressed() {
        navigateBackOrFinish()
    }
}
