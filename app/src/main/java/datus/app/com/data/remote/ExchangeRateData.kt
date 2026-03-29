package datus.app.com.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ElToqueResponse(
    val tasas: Map<String, Double>,
    val date: String,
    val hour: Int,
    val minutes: Int,
    val seconds: Int
)

@Serializable
data class DetailedExchangeRate(
    val currency: String,
    val medianRate: Double,
    val buyRate: Double,
    val sellRate: Double,
    val minBuy: Double? = null,
    val maxBuy: Double? = null,
    val minSell: Double? = null,
    val maxSell: Double? = null,
    val lastUpdated: String? = null
)

@Serializable
data class DetailedElToqueResponse(
    val rates: Map<String, DetailedExchangeRate>,
    val date: String,
    val hour: Int,
    val minutes: Int,
    val seconds: Int,
    val source: String = "elTOQUE"
)