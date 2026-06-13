package datus.app.com.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Bank {
    BANDEC,
    BPA,
    BANMET
}

class ThemeViewModel(private val context: Context) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("datus_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(
        ThemeOption.valueOf(sharedPreferences.getString("app_theme", ThemeOption.AUTO.name) ?: ThemeOption.AUTO.name)
    )
    val theme: StateFlow<ThemeOption> = _theme.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _bankCardNumber = MutableStateFlow("")
    val bankCardNumber: StateFlow<String> = _bankCardNumber.asStateFlow()

    private val _transferPin = MutableStateFlow("")
    val transferPin: StateFlow<String> = _transferPin.asStateFlow()

    private val _cardPin = MutableStateFlow("")
    val cardPin: StateFlow<String> = _cardPin.asStateFlow()

    private val _autoAuth = MutableStateFlow(false)
    val autoAuth: StateFlow<Boolean> = _autoAuth.asStateFlow()

    private val _selectedBank = MutableStateFlow<Bank?>(null)
    val selectedBank: StateFlow<Bank?> = _selectedBank.asStateFlow()

    private val _useDynamicColor = MutableStateFlow(sharedPreferences.getBoolean("dynamic_color", false))
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(sharedPreferences.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setUseDynamicColor(enabled: Boolean) {
        _useDynamicColor.value = enabled
        sharedPreferences.edit().putBoolean("dynamic_color", enabled).apply()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    init {
        loadUserData()
    }

    private fun loadUserData() {
        _phoneNumber.value = sharedPreferences.getString("phone_number", "") ?: ""
        _bankCardNumber.value = sharedPreferences.getString("bank_card_number", "") ?: ""
        _transferPin.value = sharedPreferences.getString("transfer_pin", "") ?: ""
        _cardPin.value = sharedPreferences.getString("card_pin", "") ?: ""
        _autoAuth.value = sharedPreferences.getBoolean("auto_auth", false)
        val bankName = sharedPreferences.getString("selected_bank", null)
        _selectedBank.value = bankName?.let { Bank.valueOf(it) }
    }

    fun savePhoneNumber(number: String) {
        _phoneNumber.value = number
        sharedPreferences.edit().putString("phone_number", number).apply()
    }

    fun saveBankCardNumber(number: String) {
        _bankCardNumber.value = number
        sharedPreferences.edit().putString("bank_card_number", number).apply()
    }

    fun saveTransferPin(pin: String) {
        _transferPin.value = pin
        sharedPreferences.edit().putString("transfer_pin", pin).apply()
    }

    fun saveCardPin(pin: String) {
        _cardPin.value = pin
        sharedPreferences.edit().putString("card_pin", pin).apply()
    }

    fun setAutoAuth(enabled: Boolean) {
        _autoAuth.value = enabled
        sharedPreferences.edit().putBoolean("auto_auth", enabled).apply()
    }

    fun setSelectedBank(bank: Bank?) {
        _selectedBank.value = bank
        sharedPreferences.edit().putString("selected_bank", bank?.name).apply()
    }

    fun setTheme(themeOption: ThemeOption) {
        _theme.value = themeOption
        sharedPreferences.edit().putString("app_theme", themeOption.name).apply()
    }
}

class ThemeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
