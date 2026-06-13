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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import datus.app.com.utils.dialUssdCode
import datus.app.com.ui.theme.Dimens
import datus.app.com.utils.playClickSound
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.ui.components.DatusCard
import datus.app.com.ui.components.ModernIcon
import androidx.compose.material.ExperimentalMaterialApi
import datus.app.com.viewmodel.PromotionsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlin.math.min
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass

private data class Query(val title: String, val description: String, val ussdCode: String, val icon: ImageVector)

private val queries = listOf(
    Query("Saldo Principal", "Saldo, voz, SMS, datos y vigencia", "*222#", Icons.Outlined.Info),
    Query("Consultar Límite", "Límite de recargas mensuales", "*222*732#", Icons.Outlined.VerifiedUser),
    Query("Vigencia de Datos", "Plan de datos y su vigencia", "*222*328#", Icons.Outlined.DataUsage),
    Query("Consultar Bonos", "Bonos y planes en USD", "*222*266#", Icons.Outlined.AttachMoney),
    Query("Vigencia de Voz", "Plan de voz y su vigencia", "*222*869#", androidx.compose.material.icons.Icons.Outlined.Phone),
    Query("Vigencia de SMS", "Plan de SMS y su vigencia", "*222*767#", androidx.compose.material.icons.Icons.Outlined.Email),
)

@Composable
fun QueriesScreen(navController: NavHostController, themeViewModel: ThemeViewModel) {
    val promoViewModel: PromotionsViewModel = hiltViewModel()
    val promotions by promoViewModel.promotions.collectAsStateWithLifecycle()
    val promoLoading by promoViewModel.loading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { DatusTopAppBar(title = "Consultas", navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Spacer(modifier = Modifier.height(Dimens.sm))
            DashboardSummaryRow()
            Text(
                text = "Consultas rápidas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = Dimens.md)
            )
            QueriesCompactGrid()
            if (promotions.isNotEmpty() || promoLoading) {
                PromotionsCarousel(viewModel = promoViewModel)
            }
            Spacer(modifier = Modifier.height(Dimens.lg))
        }
    }
}

@Composable
private fun DashboardSummaryRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.md),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        SummaryCard(
            icon = Icons.Outlined.Info,
            label = "Saldo",
            ussdCode = "*222#",
            gradient = listOf(Color(0xFF0061A4), Color(0xFF0099FF)),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            icon = Icons.Outlined.DataUsage,
            label = "Datos",
            ussdCode = "*222*328#",
            gradient = listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            icon = Icons.Outlined.AttachMoney,
            label = "Bono",
            ussdCode = "*222*266#",
            gradient = listOf(Color(0xFFE65100), Color(0xFFFF9800)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    icon: ImageVector,
    label: String,
    ussdCode: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) dialUssdCode(context, ussdCode)
            else Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    )

    DatusCard(
        onClick = {
            playClickSound(view)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                dialUssdCode(context, ussdCode)
            } else {
                launcher.launch(Manifest.permission.CALL_PHONE)
            }
        },
        modifier = modifier.aspectRatio(1f),
        elevation = Dimens.cardElevatedElevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(gradient)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(Dimens.iconLarge),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(Dimens.sm))
                Text(
                    text = "Ver",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun QueriesCompactGrid() {
    Column(
        modifier = Modifier.padding(horizontal = Dimens.md),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        for (rowItems in queries.chunked(3)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (query in rowItems) {
                    CompactQueryCard(
                        query = query,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompactQueryCard(query: Query, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val fontSize = remember(configuration.screenWidthDp) {
        min(13f, configuration.screenWidthDp / 28f)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) dialUssdCode(context, query.ussdCode)
            else Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    )

    DatusCard(
        onClick = {
            playClickSound(view)
            if (query.ussdCode.startsWith("smsto:")) {
                context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse(query.ussdCode) })
            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                dialUssdCode(context, query.ussdCode)
            } else {
                launcher.launch(Manifest.permission.CALL_PHONE)
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.cardCorner),
        elevation = Dimens.cardElevatedElevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            ModernIcon(
                imageVector = query.icon,
                contentDescription = null,
                containerSize = 40.dp,
                iconSize = 22.dp
            )
            Text(
                text = query.title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
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
    val lazyListState = rememberLazyListState()
    var isUserInteracting by remember { mutableStateOf(false) }

    LaunchedEffect(promotions) {
        if (promotions.size <= 1) return@LaunchedEffect
        while (true) {
            delay(10000)
            if (!isUserInteracting && !lazyListState.isScrollInProgress) {
                val current = lazyListState.firstVisibleItemIndex
                lazyListState.animateScrollToItem(index = (current + 1) % promotions.size)
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
            .wrapContentHeight()
            .pullRefresh(pullRefreshState)
    ) {
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (promotions.isNotEmpty()) {
            LazyRow(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                isUserInteracting = event.changes.any { it.pressed }
                            }
                        }
                    },
                contentPadding = PaddingValues(horizontal = Dimens.md),
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(promotions) { promotion ->
                    PromotionItem(promotion = promotion)
                }
                if (loadingMore) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxWidth().height(180.dp)
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
    val cardWidth = remember(configuration.screenWidthDp) {
        (configuration.screenWidthDp.dp - 32.dp)
    }

    DatusCard(
        modifier = Modifier
            .width(cardWidth)
            .height(260.dp), // ALTURA FIJA PARA UNIFORMIDAD TOTAL
        elevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // IMAGEN DE FONDO (O COLOR DE FALLBACK)
            if (!promotion.image_url.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(promotion.image_url).crossfade(true).size(Size(600, 400)).build()
                    ),
                    contentDescription = promotion.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
            }

            // GRADIENTE DE LEGIBILIDAD (Cubre toda la tarjeta para oscurecer suavemente la base)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.85f)
                            ),
                            startY = 100f // Comienza el gradiente un poco mas abajo para no tapar la parte superior de la imagen
                        )
                    )
            )

            // CONTENIDO DE TEXTO Y BOTÓN
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.md),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = promotion.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(Dimens.xs))
                
                Text(
                    text = promotion.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 18.sp,
                    maxLines = 3, // Limitado para no romper el diseño de altura fija, pero con espacio suficiente
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!promotion.button_text.isNullOrBlank() && !promotion.action_url.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.sm))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(promotion.action_url)))
                        },
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = promotion.button_text ?: "",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(Dimens.sm))
                }
            }
        }
    }
}
