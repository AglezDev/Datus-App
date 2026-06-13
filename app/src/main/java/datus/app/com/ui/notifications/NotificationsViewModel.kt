package datus.app.com.ui.notifications

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import datus.app.com.data.remote.Notification
import datus.app.com.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    val unreadCount: StateFlow<Int> = repository.unreadCount

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            Log.d("NotificationsViewModel", "Loading notifications...")
            try {
                val notifications = repository.getNotifications()
                Log.d("NotificationsViewModel", "Received ${notifications.size} notifications from repository.")
                val readIds = repository.getReadNotificationIds()
                val notificationsWithReadStatus = notifications.map { notification ->
                    notification.copy(isRead = readIds.contains(notification.id.toString()))
                }
                val oldestUnread = notificationsWithReadStatus
                    .firstOrNull { !it.isRead && it.id.toString() != _uiState.value.lastShownNotificationId }

                _uiState.update {
                    it.copy(
                        notifications = notificationsWithReadStatus.reversed(),
                        latestUnreadNotification = oldestUnread,
                        initialNotification = if (it.initialNotification == null && oldestUnread != null) oldestUnread else it.initialNotification,
                        isLoading = false,
                        lastShownNotificationId = oldestUnread?.id?.toString() ?: it.lastShownNotificationId
                    )
                }
                // Also refresh the global count
                repository.updateUnreadCount()
                Log.d("NotificationsViewModel", "UI State updated with ${notifications.size} notifications.")
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error loading notifications: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar notificaciones: ${e.localizedMessage ?: e.toString()}") }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        repository.markAsRead(notificationId)
        // Update local UI immediately for responsiveness
        _uiState.update { currentState ->
            val updatedNotifications = currentState.notifications.map { notification ->
                if (notification.id.toString() == notificationId) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            }
            currentState.copy(notifications = updatedNotifications)
        }
    }

    fun onPopupDismissed() {
        _uiState.update { it.copy(latestUnreadNotification = null) }
    }

    fun onInitialNotificationDismissed() {
        _uiState.update { it.copy(initialNotification = null) }
    }
}

@Immutable
data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val latestUnreadNotification: Notification? = null,
    val initialNotification: Notification? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastShownNotificationId: String? = null
)