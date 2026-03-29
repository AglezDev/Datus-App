package datus.app.com.ui.screens

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import datus.app.com.utils.NotificationScheduler
import javax.inject.Inject

val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_settings")

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _notificacionesAlIniciarEnabled = MutableStateFlow(true)
    val notificacionesAlIniciarEnabled: StateFlow<Boolean> = _notificacionesAlIniciarEnabled.asStateFlow()
    
    private val _recordatorioEnabled = MutableStateFlow(true)
    val recordatorioEnabled: StateFlow<Boolean> = _recordatorioEnabled.asStateFlow()
    
    private val _recordatorioHour = MutableStateFlow(8)
    val recordatorioHour: StateFlow<Int> = _recordatorioHour.asStateFlow()
    
    private val _recordatorioMinute = MutableStateFlow(0)
    val recordatorioMinute: StateFlow<Int> = _recordatorioMinute.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            val prefs = context.notificationDataStore.data.first()
            val recordatorioEnabled = prefs[RECORDATORIO_KEY] ?: true
            val recordatorioHour = prefs[RECORDATORIO_HOUR_KEY] ?: 8
            val recordatorioMinute = prefs[RECORDATORIO_MINUTE_KEY] ?: 0
            
            _notificacionesAlIniciarEnabled.value = prefs[NOTIFICACIONES_AL_INICIAR_KEY] ?: true
            _recordatorioEnabled.value = recordatorioEnabled
            _recordatorioHour.value = recordatorioHour
            _recordatorioMinute.value = recordatorioMinute
            
            if (recordatorioEnabled) {
                NotificationScheduler.scheduleDailyNotification(
                    context = context,
                    hour = recordatorioHour,
                    minute = recordatorioMinute,
                    title = "Datus",
                    message = "Consulta las tasas de cambio hoy",
                    requestCode = 100
                )
                Log.d("NotificationSettings", "Daily notification scheduled for $recordatorioHour:$recordatorioMinute")
            }
        }
    }
    
    fun setNotificacionesAlIniciar(enabled: Boolean) {
        viewModelScope.launch {
            context.notificationDataStore.edit { prefs ->
                prefs[NOTIFICACIONES_AL_INICIAR_KEY] = enabled
            }
            _notificacionesAlIniciarEnabled.value = enabled
        }
    }
    
    fun setRecordatorio(enabled: Boolean) {
        viewModelScope.launch {
            context.notificationDataStore.edit { prefs ->
                prefs[RECORDATORIO_KEY] = enabled
            }
            _recordatorioEnabled.value = enabled
        }
    }
    
    fun setRecordatorioHour(hour: Int) {
        viewModelScope.launch {
            context.notificationDataStore.edit { prefs ->
                prefs[RECORDATORIO_HOUR_KEY] = hour
            }
            _recordatorioHour.value = hour
        }
    }
    
    fun setRecordatorioMinute(minute: Int) {
        viewModelScope.launch {
            context.notificationDataStore.edit { prefs ->
                prefs[RECORDATORIO_MINUTE_KEY] = minute
            }
            _recordatorioMinute.value = minute
        }
    }
    
    companion object {
        private val NOTIFICACIONES_AL_INICIAR_KEY = booleanPreferencesKey("notificaciones_al_iniciar")
        private val RECORDATORIO_KEY = booleanPreferencesKey("recordatorio")
        private val RECORDATORIO_HOUR_KEY = intPreferencesKey("recordatorio_hour")
        private val RECORDATORIO_MINUTE_KEY = intPreferencesKey("recordatorio_minute")
    }
}
