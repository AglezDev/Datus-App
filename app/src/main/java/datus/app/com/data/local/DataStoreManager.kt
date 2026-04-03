package datus.app.com.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import datus.app.com.data.remote.ElToqueResponse
import datus.app.com.viewmodel.Trend
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "exchange_rates_cache")

@Singleton
class DataStoreManager @Inject constructor(private val context: Context) {
    private val EXCHANGE_RATES_KEY = stringPreferencesKey("exchange_rates")
    private val PREVIOUS_EXCHANGE_RATES_KEY = stringPreferencesKey("previous_exchange_rates")
    private val HISTORICAL_RATES_KEY = stringPreferencesKey("historical_rates")
    private val CURRENCY_TRENDS_KEY = stringPreferencesKey("currency_trends")
    private val LAST_UPDATE_KEY = longPreferencesKey("last_update_timestamp")

    // Nauta settings
    private val NAUTA_USERNAME_KEY = stringPreferencesKey("nauta_username")
    private val NAUTA_REMEMBER_ME_KEY = stringPreferencesKey("nauta_remember_me")
    private val NAUTA_AUTO_CONNECT_KEY = stringPreferencesKey("nauta_auto_connect")
    private val NAUTA_REMEMBER_WIFI_KEY = stringPreferencesKey("nauta_remember_wifi")
    private val NAUTA_RECONNECT_KEY = stringPreferencesKey("nauta_reconnect")
    private val NAUTA_USERS_KEY = stringPreferencesKey("nauta_users")
    private val NAUTA_CURRENT_WIFI_KEY = stringPreferencesKey("nauta_current_wifi")

    suspend fun saveExchangeRates(response: ElToqueResponse) {
        context.dataStore.edit { preferences ->
            preferences[EXCHANGE_RATES_KEY] = Json.encodeToString(response)
            preferences[LAST_UPDATE_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun loadExchangeRates(): ElToqueResponse? {
        val preferences = context.dataStore.data.first()
        val jsonString = preferences[EXCHANGE_RATES_KEY]
        return jsonString?.let { Json.decodeFromString<ElToqueResponse>(it) }
    }

    suspend fun getLastUpdateTimestamp(): Long {
        val preferences = context.dataStore.data.first()
        return preferences[LAST_UPDATE_KEY] ?: 0L
    }

    suspend fun savePreviousExchangeRates(response: ElToqueResponse) {
        context.dataStore.edit { preferences ->
            preferences[PREVIOUS_EXCHANGE_RATES_KEY] = Json.encodeToString(response)
        }
    }

    suspend fun loadPreviousExchangeRates(): ElToqueResponse? {
        val preferences = context.dataStore.data.first()
        val jsonString = preferences[PREVIOUS_EXCHANGE_RATES_KEY]
        return jsonString?.let { Json.decodeFromString<ElToqueResponse>(it) }
    }

    suspend fun saveHistoricalRates(historicalRates: Map<String, List<HistoricalRate>>) {
        context.dataStore.edit { preferences ->
            preferences[HISTORICAL_RATES_KEY] = Json.encodeToString(historicalRates)
        }
    }

    suspend fun loadHistoricalRates(): Map<String, List<HistoricalRate>>? {
        val preferences = context.dataStore.data.first()
        val jsonString = preferences[HISTORICAL_RATES_KEY]
        return jsonString?.let { Json.decodeFromString<Map<String, List<HistoricalRate>>>(it) }
    }

    suspend fun saveCurrencyTrends(trends: Map<String, Trend>) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_TRENDS_KEY] = Json.encodeToString(trends)
        }
    }

    suspend fun loadCurrencyTrends(): Map<String, Trend>? {
        val preferences = context.dataStore.data.first()
        val jsonString = preferences[CURRENCY_TRENDS_KEY]
        return jsonString?.let { Json.decodeFromString<Map<String, Trend>>(it) }
    }
    
    suspend fun saveNautaUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[NAUTA_USERNAME_KEY] = username
        }
    }
    
    suspend fun loadNautaUsername(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[NAUTA_USERNAME_KEY]
    }

    suspend fun saveNautaRememberMe(remember: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NAUTA_REMEMBER_ME_KEY] = remember.toString()
        }
    }

    suspend fun loadNautaRememberMe(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[NAUTA_REMEMBER_ME_KEY]?.toBoolean() ?: false
    }

    suspend fun clearNautaCredentials() {
        context.dataStore.edit { preferences ->
            preferences.remove(NAUTA_USERNAME_KEY)
            preferences.remove(NAUTA_REMEMBER_ME_KEY)
        }
    }

    // Nauta Settings - Nuevas funciones para múltiples usuarios y opciones
    suspend fun saveNautaAutoConnect(autoConnect: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NAUTA_AUTO_CONNECT_KEY] = autoConnect.toString()
        }
    }

    suspend fun loadNautaAutoConnect(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[NAUTA_AUTO_CONNECT_KEY]?.toBoolean() ?: false
    }

    suspend fun saveNautaRememberWifi(remember: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NAUTA_REMEMBER_WIFI_KEY] = remember.toString()
        }
    }

    suspend fun loadNautaRememberWifi(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[NAUTA_REMEMBER_WIFI_KEY]?.toBoolean() ?: false
    }

    suspend fun saveNautaReconnect(reconnect: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NAUTA_RECONNECT_KEY] = reconnect.toString()
        }
    }

    suspend fun loadNautaReconnect(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[NAUTA_RECONNECT_KEY]?.toBoolean() ?: false
    }

    suspend fun saveNautaUsers(users: List<NautaUser>) {
        context.dataStore.edit { preferences ->
            preferences[NAUTA_USERS_KEY] = Json.encodeToString(users)
        }
    }

    suspend fun loadNautaUsers(): List<NautaUser> {
        val preferences = context.dataStore.data.first()
        val jsonString = preferences[NAUTA_USERS_KEY]
        return jsonString?.let { Json.decodeFromString<List<NautaUser>>(it) } ?: emptyList()
    }

    suspend fun addNautaUser(user: NautaUser) {
        val users = loadNautaUsers().toMutableList()
        val existingIndex = users.indexOfFirst { it.username == user.username }
        if (existingIndex != -1) {
            users[existingIndex] = user
        } else {
            users.add(user)
        }
        saveNautaUsers(users)
    }

    suspend fun removeNautaUser(username: String) {
        val users = loadNautaUsers().filter { it.username != username }
        saveNautaUsers(users)
    }

    suspend fun saveCurrentWifi(ssid: String) {
        context.dataStore.edit { preferences ->
            preferences[NAUTA_CURRENT_WIFI_KEY] = ssid
        }
    }

    suspend fun loadCurrentWifi(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[NAUTA_CURRENT_WIFI_KEY]
    }
}

@Serializable
data class NautaUser(
    val username: String,
    val password: String,
    val isSelected: Boolean = false
)
