package datus.app.com.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean("notifications_enabled", true)
    }

    fun setMobileDataEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("mobile_data_enabled", enabled).apply()
    }

    fun isMobileDataEnabled(): Boolean {
        return prefs.getBoolean("mobile_data_enabled", false)
    }

    fun setNotifyOnUpdate(enabled: Boolean) {
        prefs.edit().putBoolean("notify_on_update", enabled).apply()
    }

    fun isNotifyOnUpdate(): Boolean {
        return prefs.getBoolean("notify_on_update", true)
    }
}
