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
}
