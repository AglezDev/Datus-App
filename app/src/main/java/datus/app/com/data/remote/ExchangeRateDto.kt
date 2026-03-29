package datus.app.com.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateDto(
    val id: String? = null,
    val currency: String,
    val rate: Double,
    val symbol: String? = null,
    val source: String? = null,
    val updatedAt: String? = null
)