package datus.app.com.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import datus.app.com.data.local.NotificationReadStatusManager
import datus.app.com.data.remote.Notification
import datus.app.com.data.remote.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationService: NotificationService,
    private val notificationReadStatusManager: NotificationReadStatusManager,
    @ApplicationContext private val context: Context
) {
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        repositoryScope.launch {
            updateUnreadCount()
        }
    }

    suspend fun getNotifications(): List<Notification> {
        if (!isNetworkAvailable()) {
            return emptyList()
        }
        return notificationService.getNotifications()
    }

    fun markAsRead(notificationId: String) {
        notificationReadStatusManager.markNotificationAsRead(notificationId)
        repositoryScope.launch {
            updateUnreadCount()
        }
    }

    fun getReadNotificationIds(): Set<String> {
        return notificationReadStatusManager.getReadNotificationIds()
    }

    suspend fun updateUnreadCount() {
        try {
            val notifications = getNotifications()
            val readIds = getReadNotificationIds()
            val count = notifications.count { !readIds.contains(it.id.toString()) }
            _unreadCount.value = count
        } catch (_: Exception) {
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}