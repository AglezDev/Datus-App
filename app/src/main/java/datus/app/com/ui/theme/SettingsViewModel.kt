package datus.app.com.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(context: Context) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("datus_settings", Context.MODE_PRIVATE)

    private val _startScreen = MutableStateFlow(sharedPreferences.getString("start_screen", "Útiles") ?: "Útiles")
    val startScreen: StateFlow<String> = _startScreen

    fun setStartScreen(screen: String) {
        viewModelScope.launch {
            sharedPreferences.edit().putString("start_screen", screen).apply()
            _startScreen.value = screen
        }
    }
}