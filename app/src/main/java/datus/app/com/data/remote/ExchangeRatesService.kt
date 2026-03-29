package datus.app.com.data.remote

import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class ExchangeRatesService @Inject constructor(private val postgrest: Postgrest) {
    
    suspend fun getExchangeRates(): List<ExchangeRateDto> {
        // In a real implementation, this would call the actual API endpoint
        // For example: return postgrest.from("exchange_rates").select().decodeList<ExchangeRateDto>()
        // For now, returning mock data that could be replaced with real API call
        return getMockExchangeRates()
    }
    
    private fun getMockExchangeRates(): List<ExchangeRateDto> {
        return listOf(
            ExchangeRateDto(
                id = "1",
                currency = "USD",
                rate = 24.0,
                symbol = "$",
                source = "BNCuba",
                updatedAt = "2025-10-05T00:00:00Z"
            ),
            ExchangeRateDto(
                id = "2", 
                currency = "EUR",
                rate = 26.5,
                symbol = "€",
                source = "BNCuba",
                updatedAt = "2025-10-05T00:00:00Z"
            ),
            ExchangeRateDto(
                id = "3",
                currency = "MLC",
                rate = 1.0,
                symbol = "CUP",
                source = "BNCuba",
                updatedAt = "2025-10-05T00:00:00Z"
            )
        )
    }
}