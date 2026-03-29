package datus.app.com.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.animation.core.animateFloat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import datus.app.com.viewmodel.ExchangeRatesViewModel
import datus.app.com.viewmodel.Trend
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MercadoScreen(
    navController: NavHostController
) {
    val exchangeRatesViewModel: ExchangeRatesViewModel = hiltViewModel()
    val exchangeRatesState by exchangeRatesViewModel.exchangeRates.collectAsStateWithLifecycle()
    val loadingState by exchangeRatesViewModel.loading.collectAsStateWithLifecycle()
    val errorState by exchangeRatesViewModel.error.collectAsStateWithLifecycle()
    val currencyTrendsState by exchangeRatesViewModel.currencyTrends.collectAsStateWithLifecycle()
    val lastUpdateTimestamp by exchangeRatesViewModel.lastUpdateTimestamp.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    val isRefreshing by exchangeRatesViewModel.loading.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullRefreshState(isRefreshing, { scope.launch { exchangeRatesViewModel.loadExchangeRates() } })

    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .collect { index ->
                if (index == 0 && !isRefreshing && exchangeRatesState != null) {
                    scope.launch {
                        exchangeRatesViewModel.loadExchangeRates()
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            DatusTopAppBar(
                title = "Mercado", 
                navController = navController, 
                canNavigateBack = true,
                actions = {
                    IconButton(onClick = { scope.launch { exchangeRatesViewModel.loadExchangeRates() } }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier
                .weight(1f)
                .pullRefresh(pullRefreshState)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Si hay datos, mostrarlos siempre (aunque haya error de conexión)
                    if (exchangeRatesState != null) {
                        val ratesList = exchangeRatesState?.tasas?.toList() ?: emptyList()
                        
                        // Orden prioritario de monedas
                        val currencyOrder = mapOf(
                            "USD" to 0, 
                            "EUR" to 1, 
                            "MLC" to 2, 
                            "CAD" to 3, 
                            "MXN" to 4, 
                            "CLA" to 5,
                            "BTC" to 6, 
                            "TRX" to 7, 
                            "USDT_TRC20" to 8, 
                            "USDT" to 9,
                            "GBP" to 10,
                            "CHF" to 11,
                            "JPY" to 12,
                            "CNY" to 13,
                            "BRL" to 14,
                            "DOP" to 15,
                            "COP" to 16,
                            "CUP" to 17
                        )
                        val orderedRates = ratesList.sortedBy { currencyOrder[it.first] ?: 100 }

                        items(orderedRates) { rate ->
                            ExchangeRateCard(
                                currency = rate.first,
                                rate = rate.second,
                                trend = currencyTrendsState[rate.first] ?: Trend.NONE,
                                navController = navController,
                                viewModel = exchangeRatesViewModel
                            )
                        }
                    } else if (loadingState) {
                        // Solo mostrar skeleton si no hay datos Y está cargando
                        items(6) {
                            ExchangeRateCardSkeleton()
                        }
                    } else if (errorState != null && exchangeRatesState == null) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudOff,
                                    contentDescription = "Error de conexión",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No se pudo actualizar",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Verifica tu conexión a internet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay datos disponibles",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
            }

            // Mostrar mensaje de "sin conexión" si hay error pero hay datos guardados
            if (errorState != null && exchangeRatesState != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sin conexión - mostrando datos guardados",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (exchangeRatesState != null && lastUpdateTimestamp > 0) {
                val dateFormat = SimpleDateFormat("dd 'de' MMMM", Locale.forLanguageTag("es-ES"))
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                
                val dateObj = Date(lastUpdateTimestamp)
                val formattedDate = dateFormat.format(dateObj).replaceFirstChar { it.uppercase() }
                val timeString = timeFormat.format(dateObj)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Actualizado: $formattedDate • $timeString",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerBrush(targetValue: Float = 1000f): androidx.compose.ui.graphics.Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    )

    val transition = androidx.compose.animation.core.rememberInfiniteTransition()
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        )
    )

    return androidx.compose.ui.graphics.Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}

@Composable
fun ExchangeRateCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header skeleton
            Spacer(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth(0.5f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ShimmerBrush())
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rate skeleton
            Spacer(
                modifier = Modifier
                    .height(28.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ShimmerBrush())
            )

            Spacer(modifier = Modifier.height(4.dp))

            // CUP label skeleton
            Spacer(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.2f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ShimmerBrush())
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Trend skeleton
            Spacer(
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth(0.35f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ShimmerBrush())
            )
        }
    }
}

@Composable
fun ExchangeRateCard(
    currency: String,
    rate: Double,
    trend: Trend = Trend.NONE,
    navController: NavHostController,
    viewModel: ExchangeRatesViewModel
) {
    val context = LocalContext.current

    data class CurrencyInfo(
        val shortName: String,
        val longName: String,
        val icon: ImageVector,
        val bgColor: Color
    )

    val currencyDetails = remember(currency) {
        when (currency) {
            "USD" -> CurrencyInfo("USD", "Dólar USA", Icons.Outlined.AttachMoney, Color(0xFF10B981))
            "EUR", "ECU" -> CurrencyInfo("EUR", "Euro", Icons.Outlined.Euro, Color(0xFF6366F1))
            "MLC" -> CurrencyInfo("MLC", "MLC", Icons.Outlined.MonetizationOn, Color(0xFFF59E0B))
            "CAD" -> CurrencyInfo("CAD", "Dólar Canadá", Icons.Outlined.AttachMoney, Color(0xFFEF4444))
            "MXN" -> CurrencyInfo("MXN", "Peso Mexicano", Icons.Outlined.Money, Color(0xFF10B981))
            "CLA" -> CurrencyInfo("CLA", "Peso Colombiano", Icons.Outlined.Money, Color(0xFF8B5CF6))
            "USDT_TRC20", "USDT" -> CurrencyInfo("USDT", "USDT", Icons.Outlined.CurrencyExchange, Color(0xFF26A17B))
            "BTC" -> CurrencyInfo("BTC", "Bitcoin", Icons.Outlined.CurrencyBitcoin, Color(0xFFF7931A))
            "TRX" -> CurrencyInfo("TRX", "TRON", Icons.Outlined.CurrencyExchange, Color(0xFFEB0029))
            "GBP" -> CurrencyInfo("GBP", "Libra Esterlina", Icons.Outlined.Euro, Color(0xFF3B82F6))
            "CHF" -> CurrencyInfo("CHF", "Franco Suizo", Icons.Outlined.Money, Color(0xFFEC4899))
            "JPY" -> CurrencyInfo("JPY", "Yen Japonés", Icons.Outlined.Money, Color(0xFFEF4444))
            "CNY" -> CurrencyInfo("CNY", "Yuan Chino", Icons.Outlined.Money, Color(0xFFEF4444))
            "BRL" -> CurrencyInfo("BRL", "Real Brasilero", Icons.Outlined.Money, Color(0xFF10B981))
            "DOP" -> CurrencyInfo("DOP", "Peso Dominicano", Icons.Outlined.Money, Color(0xFF3B82F6))
            "COP" -> CurrencyInfo("COP", "Peso Colombiano", Icons.Outlined.Money, Color(0xFFF59E0B))
            "CUP" -> CurrencyInfo("CUP", "Peso Cubano", Icons.Outlined.Money, Color(0xFF6B7280))
            else -> CurrencyInfo(currency, currency, Icons.Outlined.Money, Color(0xFF6B7280))
        }
    }

    val hasTrendData = trend != Trend.NONE
    
    val trendColor = when (trend) {
        Trend.UP -> Color(0xFF10B981)
        Trend.DOWN -> Color(0xFFEF4444)
        Trend.SAME -> Color(0xFF6B7280)
        Trend.NONE -> Color.Transparent
    }

    val trendIcon = when (trend) {
        Trend.UP -> Icons.AutoMirrored.Outlined.TrendingUp
        Trend.DOWN -> Icons.AutoMirrored.Outlined.TrendingDown
        Trend.SAME -> Icons.AutoMirrored.Outlined.TrendingFlat
        Trend.NONE -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                viewModel.saveCurrencyRate(currency, rate)
                navController.navigate("currency_history/$currency")
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with icon and currency code
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = currencyDetails.bgColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = currencyDetails.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = currencyDetails.bgColor
                        )
                        Text(
                            text = currencyDetails.shortName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = currencyDetails.bgColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rate value
                Text(
                    text = "%.2f".format(rate),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "CUP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Trend indicator icon in top-right corner - only shows when there's data
            if (hasTrendData && trendIcon != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = trendColor.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = when (trend) {
                            Trend.UP -> "Tendencia al alza"
                            Trend.DOWN -> "Tendencia a la baja"
                            Trend.SAME -> "Tendencia estable"
                            else -> null
                        },
                        modifier = Modifier
                            .padding(4.dp)
                            .size(20.dp),
                        tint = trendColor
                    )
                }
            }
        }
    }
}