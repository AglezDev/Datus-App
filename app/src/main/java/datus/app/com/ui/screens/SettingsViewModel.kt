package datus.app.com.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import datus.app.com.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _notificationsEnabled = MutableStateFlow(settingsRepository.areNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _mobileDataEnabled = MutableStateFlow(settingsRepository.isMobileDataEnabled())
    val mobileDataEnabled: StateFlow<Boolean> = _mobileDataEnabled.asStateFlow()

    private val _notifyOnUpdate = MutableStateFlow(settingsRepository.isNotifyOnUpdate())
    val notifyOnUpdate: StateFlow<Boolean> = _notifyOnUpdate.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
        _notificationsEnabled.value = enabled
    }

    fun setMobileDataEnabled(enabled: Boolean) {
        settingsRepository.setMobileDataEnabled(enabled)
        _mobileDataEnabled.value = enabled
    }

    fun setNotifyOnUpdate(enabled: Boolean) {
        settingsRepository.setNotifyOnUpdate(enabled)
        _notifyOnUpdate.value = enabled
    }
}
