package datus.app.com.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import datus.app.com.data.db.TasaHistorica
import datus.app.com.data.db.TasaHistoricaDao
import datus.app.com.data.local.DataStoreManager
import datus.app.com.data.local.HistoricalRate
import datus.app.com.data.remote.ElToqueResponse
import datus.app.com.BuildConfig
import datus.app.com.repository.SettingsRepository
import datus.app.com.utils.LocalNotificationHelper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
enum class Trend {
    UP, DOWN, SAME, NONE
}

@HiltViewModel
class ExchangeRatesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: HttpClient,
    private val dataStoreManager: DataStoreManager,
    private val tasaHistoricaDao: TasaHistoricaDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    init {
        LocalNotificationHelper.createNotificationChannel(context)
    }

    // Debounce para scraping: mínimo 1 minuto entre requests
    @Volatile
    private var lastScrapeTime = 0L
    private val MIN_SCRAPE_INTERVAL = 60_000L // 1 minuto

    private var isLoadingRates = false

    private fun hasInternetConnection(): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            if (network == null) {
                Log.d("ExchangeRatesViewModel", "No active network")
                return false
            }
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities == null) {
                Log.d("ExchangeRatesViewModel", "No network capabilities")
                return false
            }
            val hasConnection = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            Log.d("ExchangeRatesViewModel", "hasConnection: $hasConnection, wifi: ${capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)}, cellular: ${capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)}")
            return hasConnection
        } catch (e: Exception) {
            Log.e("ExchangeRatesViewModel", "Error checking connection: ${e.message}")
            return true // Allow update on error
        }
    }

    private fun canUpdate(): Boolean {
        val result = hasInternetConnection()
        Log.d("ExchangeRatesViewModel", "canUpdate: $result")
        return result
    }

    fun saveCurrencyRate(currency: String, rate: Double) {
        viewModelScope.launch {
            try {
                tasaHistoricaDao.insert(
                    TasaHistorica(
                        currency = currency,
                        tasa = rate,
                        timestamp = System.currentTimeMillis()
                    )
                )
                Log.d("ExchangeRatesViewModel", "Saved rate for $currency: $rate")
            } catch (e: Exception) {
                Log.e("ExchangeRatesViewModel", "Error saving rate: ${e.message}")
            }
        }
    }

    private val _exchangeRates = MutableStateFlow<ElToqueResponse?>(null)
    val exchangeRates: StateFlow<ElToqueResponse?> = _exchangeRates

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currencyTrends = MutableStateFlow<Map<String, Trend>>(emptyMap())
    val currencyTrends: StateFlow<Map<String, Trend>> = _currencyTrends

    private val _lastUpdateTimestamp = MutableStateFlow<Long>(0L)
    val lastUpdateTimestamp: StateFlow<Long> = _lastUpdateTimestamp


    private val AUTHORIZATION_TOKEN = run {
        val key = BuildConfig.ELTOQUE_KEY
        if (key.isNotEmpty() && key != "\"\"") {
            Log.d("ExchangeRatesViewModel", "Using API key from BuildConfig")
            key
        } else {
            Log.e("ExchangeRatesViewModel", "ELTOQUE_KEY is missing or empty. Please set it in local.properties as ELTOQUE_KEY")
            // Provide a fallback message that can help in debugging
            ""
        }
    }
    private val BASE_URL = "https://tasas.eltoque.com"

    init {
        Log.d("ExchangeRatesViewModel", "ViewModel initialized")
        loadInitialData()
    }

    private fun loadInitialData() {
        Log.d("ExchangeRatesViewModel", "loadInitialData: START")
        viewModelScope.launch {
            _loading.value = true
            _error.value = null // Clear error on initial load
            try {
                val cachedData = dataStoreManager.loadExchangeRates()
                Log.d(
                    "ExchangeRatesViewModel",
                    "loadInitialData: cachedData is ${if (cachedData == null) "null" else "not null"}"
                )
                if (cachedData != null && isValidRates(cachedData)) {
                    _exchangeRates.value = cachedData
                    _lastUpdateTimestamp.value = dataStoreManager.getLastUpdateTimestamp()
                    _error.value = null // Clear error if cached data is valid
                    Log.d(
                        "ExchangeRatesViewModel",
                        "loadInitialData: Calculating trends for cached data..."
                    )
                    // Calculate trends comparing with previous rates
                    _currencyTrends.value = calculateTrendsWithPrevious(cachedData)
                    Log.d(
                        "ExchangeRatesViewModel",
                        "loadInitialData: Trends calculated for cached data."
                    )
                } else if (cachedData != null) {
                    // Cached data is invalid (1=1), try fallback but don't show error
                    val fallbackRates = createFallbackRates()
                    _exchangeRates.value = fallbackRates
                    _lastUpdateTimestamp.value = System.currentTimeMillis()
                    _currencyTrends.value = calculateTrendsWithPrevious(fallbackRates)
                    dataStoreManager.savePreviousExchangeRates(fallbackRates)
                }
            } catch (e: Exception) {
                Log.e(
                    "ExchangeRatesViewModel",
                    "loadInitialData: Exception while loading from cache: ${e.message}",
                    e
                )
            }
            _loading.value = false
            Log.d(
                "ExchangeRatesViewModel",
                "loadInitialData: FINISHED. Triggering network refresh."
            )
            loadExchangeRates()
        }
    }

    fun loadExchangeRates() {
        Log.d("ExchangeRatesViewModel", "loadExchangeRates: START")

        // Evitar llamadas concurrentes
        if (isLoadingRates) {
            Log.d("ExchangeRatesViewModel", "Load already in progress, skipping")
            return
        }

        // Debounce: verificar si se hizo scraping recientemente
        val now = System.currentTimeMillis()
        if (now - lastScrapeTime < MIN_SCRAPE_INTERVAL) {
            Log.d("ExchangeRatesViewModel", "Scrape throttled, last scrape was ${now - lastScrapeTime}ms ago")
            return
        }

        // Check if we can update based on network connection
        if (!canUpdate()) {
            Log.d("ExchangeRatesViewModel", "Update blocked: No internet connection detected")
            viewModelScope.launch {
                _loading.value = true
                try {
                    loadFromCacheOrFallback()
                } finally {
                    _loading.value = false
                }
            }
            return
        }

        isLoadingRates = true
        lastScrapeTime = now

        viewModelScope.launch {
            try {
                _loading.value = true

                // Try scraping first
                val scrapedData = scrapeExchangeRates()

                // Check if scraped data is valid (not 1=1)
                val hasInvalidRates = scrapedData != null && !isValidRates(scrapedData)

                // Only use scraped data if it's valid
                if (scrapedData != null && isValidRates(scrapedData)) {
                    // Clear error if data is valid
                    _error.value = null
                    Log.d("ExchangeRatesViewModel", "loadExchangeRates: Scraped data is valid: $scrapedData")

                    // Send notification if enabled
                    if (settingsRepository.isNotifyOnUpdate()) {
                        val usdRate = scrapedData.tasas["USD"]
                        LocalNotificationHelper.showRatesUpdatedNotification(context, usdRate)
                    }

                    // Calculate trends by comparing with previous rates
                    val trends = calculateTrendsWithPrevious(scrapedData)
                    _currencyTrends.value = trends

                    _exchangeRates.value = scrapedData
                    dataStoreManager.saveExchangeRates(scrapedData)
                    _lastUpdateTimestamp.value = System.currentTimeMillis()
                    Log.d("ExchangeRatesViewModel", "loadExchangeRates: Saved scraped rates to DataStore.")

                    // Save current rates as previous for next comparison
                    dataStoreManager.savePreviousExchangeRates(scrapedData)

                    return@launch
                }

                // If scraping fails or returns invalid data, try to use cached data first
                Log.d("ExchangeRatesViewModel", "Scraping failed or returned invalid data, trying cached data")
                val cachedData = dataStoreManager.loadExchangeRates()

                if (cachedData != null && isValidRates(cachedData)) {
                    // Use cached data if available and valid
                    Log.d("ExchangeRatesViewModel", "Using cached exchange rates")
                    _exchangeRates.value = cachedData
                    _currencyTrends.value = calculateTrendsWithPrevious(cachedData)
                    _lastUpdateTimestamp.value = System.currentTimeMillis()
                    _error.value = null
                } else {
                    // Only use fallback if no valid cache exists
                    Log.d("ExchangeRatesViewModel", "No valid cache, using fallback rates")
                    val fallbackRates = createFallbackRates()
                    _exchangeRates.value = fallbackRates
                    _currencyTrends.value = calculateTrendsWithPrevious(fallbackRates)
                    _lastUpdateTimestamp.value = System.currentTimeMillis()
                    dataStoreManager.savePreviousExchangeRates(fallbackRates)
                }

                Log.d("ExchangeRatesViewModel", "loadExchangeRates: FINISHED")
            } finally {
                _loading.value = false
                isLoadingRates = false
            }
        }
    }

    private suspend fun loadFromCacheOrFallback() {
        try {
            val cachedData = dataStoreManager.loadExchangeRates()
            if (cachedData != null && isValidRates(cachedData)) {
                _exchangeRates.value = cachedData
                _currencyTrends.value = calculateTrendsWithPrevious(cachedData)
            } else {
                val fallbackRates = createFallbackRates()
                _exchangeRates.value = fallbackRates
                _currencyTrends.value = calculateTrendsWithPrevious(fallbackRates)
            }
        } catch (e: Exception) {
            Log.e("ExchangeRatesViewModel", "Error loading cached data: ${e.message}")
            val fallbackRates = createFallbackRates()
            _exchangeRates.value = fallbackRates
            _currencyTrends.value = calculateTrendsWithPrevious(fallbackRates)
        }
    }

    private suspend fun calculateTrends(
        current: ElToqueResponse
    ): Map<String, Trend> {
        Log.d("ExchangeRatesViewModel", "calculateTrends: START")
        val trends = mutableMapOf<String, Trend>()
        val twentyFourHoursAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000

        val currencies = current.tasas.keys.toList()
        val minMaxRates = try {
            tasaHistoricaDao.getMinMaxRates(currencies, twentyFourHoursAgo)
        } catch (e: Exception) {
            Log.e("ExchangeRatesViewModel", "Error fetching min/max rates: ${e.message}")
            emptyList()
        }

        val minMaxMap = minMaxRates.associateBy { it.currency }

        current.tasas.forEach { (currency, currentValue) ->
            val rates = minMaxMap[currency]
            val max24h = rates?.maxRate
            val min24h = rates?.minRate

            if (max24h == null || min24h == null || max24h == min24h) {
                trends[currency] = Trend.NONE
                return@forEach
            }

            val range = max24h - min24h
            if (range == 0.0) {
                trends[currency] = Trend.SAME
                return@forEach
            }

            val position = (currentValue - min24h) / range

            trends[currency] = when {
                position > 0.7 -> Trend.UP
                position < 0.3 -> Trend.DOWN
                else -> Trend.SAME
            }
        }
        Log.d("ExchangeRatesViewModel", "calculateTrends: FINISHED")
        return trends
    }

    // Calculate trends by comparing current rates with previous saved rates
    private suspend fun calculateTrendsWithPrevious(current: ElToqueResponse): Map<String, Trend> {
        val trends = mutableMapOf<String, Trend>()
        
        try {
            // Load previous rates from DataStore
            val previousRates = dataStoreManager.loadPreviousExchangeRates()
            
            if (previousRates != null) {
                Log.d("ExchangeRatesViewModel", "calculateTrendsWithPrevious: Comparing with previous rates")
                
                current.tasas.forEach { (currency, currentValue) ->
                    val previousValue = previousRates.tasas[currency]
                    
                    if (previousValue != null) {
                        val difference = currentValue - previousValue
                        val percentChange = if (previousValue != 0.0) (difference / previousValue) * 100 else 0.0
                        
                        trends[currency] = when {
                            percentChange > 0.5 -> Trend.UP    // More than 0.5% increase
                            percentChange < -0.5 -> Trend.DOWN  // More than 0.5% decrease
                            else -> Trend.SAME
                        }
                        Log.d("ExchangeRatesViewModel", "Trend for $currency: $currentValue vs $previousValue = ${percentChange}%")
                    } else {
                        trends[currency] = Trend.NONE  // No previous data
                    }
                }
            } else {
                // No previous data, mark all as NONE
                current.tasas.keys.forEach { currency ->
                    trends[currency] = Trend.NONE
                }
                Log.d("ExchangeRatesViewModel", "calculateTrendsWithPrevious: No previous rates found")
            }
        } catch (e: Exception) {
            Log.e("ExchangeRatesViewModel", "Error calculating trends with previous: ${e.message}")
            // If error, return NONE for all
            current.tasas.keys.forEach { currency ->
                trends[currency] = Trend.NONE
            }
        }
        
        return trends
    }

    // Validate that rates are reasonable (all > 1 to avoid 1=1 issue)
    private fun isValidRates(rates: ElToqueResponse?): Boolean {
        if (rates == null) return false
        val tasas = rates.tasas
        if (tasas.isEmpty()) return false
        
        // Check if USD rate is valid (should be > 100 for CUP)
        val usdRate = tasas["USD"] ?: return false
        if (usdRate <= 1 || usdRate < 100) {
            Log.w("ExchangeRatesViewModel", "Invalid USD rate: $usdRate (likely 1=1)")
            return false
        }
        
        // Check for any rate that is suspiciously low
        tasas.values.forEach { value ->
            if (value <= 1) {
                Log.w("ExchangeRatesViewModel", "Invalid rate detected: $value (likely 1=1)")
                return false
            }
        }
        
        return true
    }

    private fun createFallbackRates(): ElToqueResponse {
        // Fallback rates from eltoque.com (user provided data)
        return ElToqueResponse(
            tasas = mapOf(
                "USD" to 515.0,
                "EUR" to 580.0,
                "MLC" to 400.0,
                "CAD" to 337.65,
                "MXN" to 26.74,
                "CLA" to 503.55
            ),
            date = "Datos de respaldo",
            hour = 0,
            minutes = 0,
            seconds = 0
        )
    }

    private suspend fun scrapeExchangeRates(): ElToqueResponse? = withContext(Dispatchers.IO) {
        try {
            Log.d("ExchangeRatesViewModel", "Scraping rates from eltoque.com...")
            val doc = Jsoup.connect("https://eltoque.com/tasas-de-cambio-cuba")
                .userAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .timeout(20000)
                .get()

            val tasas = mutableMapOf<String, Double>()

            // Try multiple selectors to find the rates table
            // Pattern 1: span with id starting with cell-title
            val cells = doc.select("span[id^=cell-title-v2]")
            Log.d("ExchangeRatesViewModel", "Found ${cells.size} cells with id starting with cell-title-v2")
            
            for (cell in cells) {
                val row = cell.parent()?.parent()
                val currencyText = cell.text().replace("1", "").trim()
                val valueElement = row?.select("span.font-extrabold.text-lg, span.font-extrabold")?.firstOrNull()
                val valueText = valueElement?.text()?.replace(",", ".")?.replace("[^0-9.]".toRegex(), "")
                val value = valueText?.toDoubleOrNull()
                
                if (currencyText.isNotEmpty() && value != null) {
                    tasas[currencyText] = value
                    Log.d("ExchangeRatesViewModel", "Scraped: $currencyText = $value CUP")
                }
            }

            // If still empty, try alternative approach - look for all currency values
            if (tasas.isEmpty()) {
                Log.d("ExchangeRatesViewModel", "Trying alternative scraping method...")
                // Look for patterns like "515.00 CUP" or "1 USD"
                val allSpans = doc.select("span")
                for (span in allSpans) {
                    val text = span.text()
                    if (text.matches(Regex(".*\\d+\\.?\\d*\\s*CUP"))) {
                        val match = Regex("(\\d+\\.?\\d*)\\s*CUP").find(text)
                        match?.let {
                            val cupValue = it.groupValues[1].toDoubleOrNull()
                            // Try to find the currency in nearby elements
                            val parent = span.parent()?.parent()
                            val currencyCell = parent?.select("span[id^=cell-title]")?.firstOrNull()
                            val currency = currencyCell?.text()?.replace("1", "")?.trim()
                            if (currency != null && cupValue != null) {
                                tasas[currency] = cupValue
                                Log.d("ExchangeRatesViewModel", "Scraped (alt): $currency = $cupValue CUP")
                            }
                        }
                    }
                }
            }

            if (tasas.isNotEmpty()) {
                Log.d("ExchangeRatesViewModel", "Successfully scraped ${tasas.size} rates")
                ElToqueResponse(
                    tasas = tasas,
                    date = "Scraped",
                    hour = 0,
                    minutes = 0,
                    seconds = 0
                )
            } else {
                Log.w("ExchangeRatesViewModel", "No rates scraped from page, HTML length: ${doc.html().length}")
                null
            }
        } catch (e: Exception) {
            Log.e("ExchangeRatesViewModel", "Error scraping rates: ${e.message}", e)
            null
        }
    }
}