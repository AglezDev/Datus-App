package datus.app.com.data.local

import kotlinx.serialization.Serializable

@Serializable
data class HistoricalRate(
    val timestamp: Long,
    val value: Double
)