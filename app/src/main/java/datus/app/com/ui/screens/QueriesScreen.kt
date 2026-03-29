@file:OptIn(ExperimentalMaterial3Api::class)
package datus.app.com.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import datus.app.com.utils.dialUssdCode
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalView
import datus.app.com.utils.playClickSound
import androidx.lifecycle.viewmodel.compose.viewModel
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.ui.theme.ThemeViewModelFactory
import androidx.compose.material.ExperimentalMaterialApi
import datus.app.com.viewmodel.PromotionsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.delay
import kotlin.math.min

data class Query(val title: String, val description: String, val ussdCode: String, val icon: ImageVector)


val queries = listOf(
    Query("Saldo Principal", "Consulta saldo principal, voz, SMS, datos y vigencia de la línea.", "*222#", Icons.Outlined.Info),
    Query("Consultar Límite", "Consulta tu límite de recargas mensuales.", "*222*732#", Icons.Outlined.VerifiedUser),
    Query("Vigencia de Datos", "Consulta tu plan de datos y su vigencia.", "*222*328#", Icons.Outlined.DataUsage),
    Query("Consultar Bonos", "Consulta tus bonos y planes en USD.", "*222*266#", Icons.Outlined.AttachMoney),
    Query("Vigencia de Voz", "Consulta tu plan de voz y su vigencia.", "*222*869#", Icons.Outlined.Call),
    Query("Vigencia de SMS", "Consulta tu plan de SMS y su vigencia.", "*222*767#", Icons.Outlined.Sms),
)

@Composable
fun QueriesScreen(navController: NavHostController, themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(LocalContext.current))) {
    Scaffold(
        topBar = { DatusTopAppBar(title = "Consultas", navController = navController,) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            QueriesGrid()
            Spacer(modifier = Modifier.weight(1f))
            PromotionsCarousel()
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun QueriesGrid() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val chunkedQueries = queries.chunked(2)
        for (rowItems in chunkedQueries) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (query in rowItems) {
                    Box(modifier = Modifier.weight(1f)) {
                        QueryCard(query = query)
                    }
                }
                if (rowItems.size < 2) { // Add a spacer for the last row if it's not full
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun QueryCard(query: Query) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val adaptiveFontSize = min(16f, configuration.screenWidthDp / 22f)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                dialUssdCode(context, query.ussdCode)
            } else {
                Toast.makeText(context, "Permiso para realizar llamadas denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clickable {
                playClickSound(view)
                if (query.ussdCode.startsWith("smsto:")) {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse(query.ussdCode)
                    context.startActivity(intent)
                } else {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) -> {
                            dialUssdCode(context, query.ussdCode)
                        }
                        else -> {
                            launcher.launch(Manifest.permission.CALL_PHONE)
                        }
                    }
                }
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = query.icon,
                contentDescription = "Query Icon",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = query.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = adaptiveFontSize.sp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PromotionsCarousel(modifier: Modifier = Modifier, viewModel: PromotionsViewModel = hiltViewModel()) {
    val promotions by viewModel.promotions.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val loadingMore by viewModel.loadingMore.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = promotions) {
        if (promotions.size > 1) {
            while (true) {
                delay(10000) // 10 seconds
                val currentScrollPosition = lazyListState.firstVisibleItemIndex
                val nextIndex = (currentScrollPosition + 1) % promotions.size
                lazyListState.animateScrollToItem(index = nextIndex)
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = { viewModel.loadPromos() }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // Adjusted height for the taller card
            .pullRefresh(pullRefreshState)
    ) {
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (promotions.isNotEmpty()) {
            LazyRow(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(promotions) { promotion ->
                    // Directly use the redesigned PromotionItem
                    PromotionItem(promotion = promotion)
                }
                
                if (loadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .height(180.dp) // Keep loader height consistent
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            
            LaunchedEffect(promotions.size) {
                if (promotions.size >= 7 && !loading && !loadingMore) {
                    viewModel.loadMorePromos()
                }
            }
        }


    }
}

@Composable
fun PromotionItem(promotion: Promotion) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = screenWidth - 32.dp 

    val titleFontSize = min(16f, configuration.screenWidthDp / 22f)
    val descriptionFontSize = min(14f, configuration.screenWidthDp / 26f)
    val buttonFontSize = min(12f, configuration.screenWidthDp / 28f)

    Card(
        modifier = Modifier
            .width(cardWidth)
            .aspectRatio(1.8f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            if (!promotion.image_url.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(promotion.image_url)
                            .crossfade(true)
                            .size(Size(400, 300))
                            .build()
                    ),
                    contentDescription = promotion.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color.Gray))
            }

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // Content: Text and Button
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Text section that takes up available space
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = promotion.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFontSize.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = promotion.description,
                        fontSize = descriptionFontSize.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Button at the bottom
                if (!promotion.button_text.isNullOrBlank() && !promotion.action_url.isNullOrBlank()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(promotion.action_url))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = try {
                                Color(android.graphics.Color.parseColor(promotion.color ?: "#007bff"))
                            } catch (_: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = promotion.button_text ?: "",
                            fontSize = buttonFontSize.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}