package datus.app.com.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationReadStatusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val PREFS_NAME = "notification_prefs"
    private val KEY_READ_NOTIFICATIONS = "read_notification_ids"

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getReadNotificationIds(): Set<String> {
        return sharedPreferences.getStringSet(KEY_READ_NOTIFICATIONS, emptySet()) ?: emptySet()
    }

    fun markNotificationAsRead(notificationId: String) {
        val currentReadIds = getReadNotificationIds().toMutableSet()
        if (currentReadIds.add(notificationId)) {
            sharedPreferences.edit().putStringSet(KEY_READ_NOTIFICATIONS, currentReadIds).apply()
        }
    }
}
