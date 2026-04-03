package datus.app.com.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.draw.clip
import datus.app.com.Plan
import kotlinx.coroutines.launch
import datus.app.com.utils.dialUssdCode
import androidx.navigation.NavHostController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalView

import androidx.compose.ui.res.painterResource
import datus.app.com.R
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.utils.playClickSound
import kotlin.math.min
import androidx.compose.ui.platform.LocalConfiguration


// --- Datos de ejemplo ---
val dataPlans = listOf(
    Plan("Plan de 2GB", "2 GB + 15 min + 20 SMS + 300 MB Nacionales", "120 CUP", "*133*1*4*2*1#"),
    Plan("Plan de 4GB", "4 GB + 35 min + 40 SMS + 300 MB Nacionales", "240 CUP", "*133*1*4*3*1#"),
    Plan("Plan de 4.5GB", "4.5 GB + 300 MB Nacionales", "240 CUP", "*133*1*4*1*1#"),
    Plan("Plan de 6GB", "6 GB + 60 min + 70 SMS + 300 MB Nacionales", "360 CUP", "*133*1*4*4*1#")
)
val callPlans = listOf(
    Plan("Plan de 5 MIN", "5 minutos de llamadas", "37.50 CUP", "*133*3*1*1#"),
    Plan("Plan de 10 MIN", "10 minutos de llamadas", "72.50 CUP", "*133*3*2*1#"),
    Plan("Plan de 15 MIN", "15 minutos de llamadas", "105.00 CUP", "*133*3*3*1#"),
    Plan("Plan de 25 MIN", "25 minutos de llamadas", "162.50 CUP", "*133*3*4*1#"),
    Plan("Plan de 40 MIN", "40 minutos de llamadas", "250.00 CUP", "*133*3*5*1#")
)
val messagePlans = listOf(
    Plan("Plan de 20 SMS", "20 SMS Nacionales", "15 CUP", "*133*2*1*1#"),
    Plan("Plan de 50 SMS", "50 SMS Nacionales", "30 CUP", "*133*2*2*1#"),
    Plan("Plan de 90 SMS", "90 SMS Nacionales", "50 CUP", "*133*2*3*1#"),
    Plan("Plan de 120 SMS", "120 SMS Nacionales", "60 CUP", "*133*2*4*1#")
)

val otherPlans = listOf(
    Plan("Bolsa Diaria", "200 MB de Datos", "25 CUP", "*133*1*3*1#"),
    Plan("Plan ToDus", "200 MB de datos Nacionales", "25 CUP", "*133*1*2*1#")
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(navController: NavHostController, themeViewModel: ThemeViewModel) {
    val tabs = listOf("Datos", "Voz", "SMS", "Otros")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    Scaffold(
        topBar = { DatusTopAppBar(title = "Planes", navController = navController,) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Match the card width with the same 16dp horizontal padding
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                indicator = {},
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(50))
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = pagerState.currentPage == index
                    val configuration = LocalConfiguration.current
                    val adaptiveFontSize = min(14f, configuration.screenWidthDp / 25f)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        shadowElevation = if (selected) 0.dp else 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp, max = 56.dp)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                playClickSound(view)
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            },
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp), // Original horizontal padding
                                fontSize = adaptiveFontSize.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> PlanList(plans = dataPlans, planType = 0)
                    1 -> PlanList(plans = callPlans, planType = 1)
                    2 -> PlanList(plans = messagePlans, planType = 2)
                    3 -> PlanList(plans = otherPlans, planType = 3)
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun PlanList(plans: List<Plan>, planType: Int) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<Plan?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            selectedPlan?.let { dialUssdCode(context, it.ussdCode) }
        } else {
            Toast.makeText(context, "Permiso para realizar llamadas denegado", Toast.LENGTH_SHORT).show()
        }
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            onConfirm = {
                showConfirmationDialog = false
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) -> {
                        selectedPlan?.let { dialUssdCode(context, it.ussdCode) }
                    }
                    else -> {
                        launcher.launch(Manifest.permission.CALL_PHONE)
                    }
                }
            },
            onDismiss = { showConfirmationDialog = false },
            title = "Confirmar Compra",
            message = "¿Estás seguro de que deseas comprar:  ${selectedPlan?.name}?\n\nEsta operación descontará saldo de su línea movil."
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(plans, key = { it.name }) { plan -> 
            PlanCard(plan = plan, planType = planType) {
                selectedPlan = plan
                showConfirmationDialog = true
            }
        }
    }
}



@Composable
fun PlanCard(plan: Plan, planType: Int, onClick: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val adaptiveTitleSize = min(20f, configuration.screenWidthDp / 18f)
    val adaptiveIconSize = min(32f, configuration.screenWidthDp / 11f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                playClickSound(view)
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (planType == 0) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_data_usage),
                        contentDescription = "Plan Icon",
                        modifier = Modifier.size(adaptiveIconSize.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (planType == 1) {
                    Icon(
                        painter = painterResource(id = R.drawable.call_24px),
                        contentDescription = "Plan Icon",
                        modifier = Modifier.size(adaptiveIconSize.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (planType == 2) {
                    Icon(
                        painter = painterResource(id = R.drawable.chat_bubble_24px),
                        contentDescription = "Plan Icon",
                        modifier = Modifier.size(adaptiveIconSize.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    val icon = when (plan.name) {
                        "Bolsa Diaria" -> Icons.Filled.Today
                        "Plan ToDus" -> Icons.Filled.Redeem
                        else -> when (planType) {
                            0 -> Icons.Filled.DataUsage
                            else -> Icons.Filled.MoreHoriz
                        }
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Plan Icon",
                        modifier = Modifier.size(adaptiveIconSize.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = adaptiveTitleSize.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (plan.price.isNotEmpty()) {
                PriceTag(price = plan.price, modifier = Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@Composable
fun PriceTag(price: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = price,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

fun shareApp(context: Context) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "¡Descarga Datus, la mejor app para gestionar tus servicios de ETECSA!\n\nhttps://github.com/ADNova-Design/Datus-App/releases/latest/download/Datus-v2.1.2.apk")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

fun openWhatsAppSupport(context: Context) {
    val phoneNumber = "+5359072053" // Reemplazar con tu número de WhatsApp
    val message = "Hola, necesito ayuda con la app Datus."
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Opcional: Mostrar un Toast o mensaje si WhatsApp no está instalado
    }
}