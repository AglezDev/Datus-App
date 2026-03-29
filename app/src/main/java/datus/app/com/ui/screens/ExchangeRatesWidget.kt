package datus.app.com.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import datus.app.com.data.remote.ElToqueResponse
import java.text.DecimalFormat

// Composable for the exchange rates widget - Single horizontal bar layout
@Composable
fun ExchangeRatesWidget(
    modifier: Modifier = Modifier,
    exchangeRates: ElToqueResponse? = null,
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Refresh button
            IconButton(
                onClick = onRefresh,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar tasas",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Currency information taking the rest of the space
            if (exchangeRates != null) {
                // Use a Row with equal space distribution for each currency
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Limit to USD, EUR, and MLC as requested
                    val usdRate = exchangeRates.tasas["USD"]
                    val eurRate = exchangeRates.tasas["ECU"] ?: exchangeRates.tasas["EUR"] // ECU might be used instead of EUR
                    val mlcRate = exchangeRates.tasas["MLC"] // MLC might not be available from elTOQUE
                
                    val currenciesToShow = buildList {
                        usdRate?.let { 
                            add(Pair("USD", it)) 
                        }
                        eurRate?.let { 
                            add(Pair("EUR", it)) 
                        }
                        mlcRate?.let { 
                            add(Pair("MLC", it)) 
                        }
                    }
                    
                    // Show each currency item with equal weight
                    currenciesToShow.forEach { (currency, rate) ->
                        Box(modifier = Modifier.weight(1f)) {
                            ExchangeRateItem(
                                currency = currency,
                                rate = rate
                            )
                        }
                    }
                    
                    // Add empty boxes for missing currencies to maintain consistent layout
                    val missingCurrencies = 3 - currenciesToShow.size
                    repeat(missingCurrencies) {
                        Box(modifier = Modifier.weight(1f)) {
                            ExchangeRateItem(
                                currency = "", 
                                rate = 0.0
                            )
                        }
                    }
                }
            } else {
                // Show placeholder when no data
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExchangeRateItem(currency = "USD", rate = 0.0)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ExchangeRateItem(currency = "EUR", rate = 0.0)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ExchangeRateItem(currency = "MLC", rate = 0.0)
                    }
                }
            }
        }
    }
}

// Composable for individual exchange rate item
@Composable
fun ExchangeRateItem(
    currency: String,
    rate: Double
) {
    val formattedRate = DecimalFormat("#.##").format(rate)
    
    Card(
        modifier = Modifier
            .fillMaxSize(), // Use fillMaxSize instead of weight to fill the Box it's in
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currency,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 10.sp
            )
            Text(
                text = formattedRate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 11.sp
            )
        }
    }
}