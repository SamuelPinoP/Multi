package com.example.multi.navigation

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/**
 * Determines which screen should be shown when the application starts.
 */
class AppStartDestinationResolver(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun resolve(): AppDestination = when {
        !prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false) -> AppDestination.Onboarding
        prefs.getBoolean(KEY_APP_LOCK_ENABLED, false) -> AppDestination.Lock
        else -> AppDestination.Home
    }

    fun markOnboardingComplete() {
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETE, true) }
    }

    fun enableLock() {
        prefs.edit { putBoolean(KEY_APP_LOCK_ENABLED, true) }
    }

    fun disableLock() {
        prefs.edit { putBoolean(KEY_APP_LOCK_ENABLED, false) }
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
    }
}
