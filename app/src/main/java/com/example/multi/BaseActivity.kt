package com.example.multi

import android.content.Intent
import androidx.activity.ComponentActivity

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
            startActivity(Intent(this, fallbackActivity()))
        }
        finish()
    }

    /**
     * Provides the fallback activity that should be launched when this
     * activity is the root of the task and the user navigates back. Subclasses
     * can override this to customize the back navigation destination.
     */
    protected open fun fallbackActivity(): Class<out ComponentActivity> = MainActivity::class.java

    override fun onBackPressed() {
        navigateBackOrFinish()
    }
}
