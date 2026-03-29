package datus.app.com.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import datus.app.com.data.db.TasaHistorica
import datus.app.com.viewmodel.CurrencyHistoryViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CurrencyHistoryScreen(
    currency: String,
    navController: NavHostController,
    viewModel: CurrencyHistoryViewModel = hiltViewModel()
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val displayCurrency = if (currency == "ECU") "EUR" else currency
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Group history by date - safely handle empty or null history
    val groupedHistory = try {
        history
            .filter { it.timestamp > 0 }
            .groupBy {
                try {
                    Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                } catch (e: Exception) {
                    LocalDate.now()
                }
            }
    } catch (e: Exception) {
        emptyMap()
    }

    Scaffold(
        topBar = {
            DatusTopAppBar(
                title = "Historial $displayCurrency",
                navController = navController,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Eliminar historial",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                shape = RoundedCornerShape(28.dp),
                title = { Text("Eliminar historial") },
                text = { Text("¿Estás seguro de que deseas eliminar todo el historial de $displayCurrency? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteHistory()
                            showDeleteDialog = false
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (groupedHistory.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay historial para esta moneda.")
                    }
                }
            } else {
                groupedHistory.forEach { (date, records) ->
                    stickyHeader {
                        Text(
                            text = formatDateHeader(date),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(records.distinctBy { it.tasa }) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            ListItem(
                                headlineContent = {
                                    val itemDisplayCurrency = if (record.currency == "ECU") "EUR" else record.currency
                                    Text(
                                        text = "1 $itemDisplayCurrency = ${"%.2f".format(record.tasa)} CUP",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(record.timestamp))
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDateHeader(date: LocalDate): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    
    return when (date) {
        today -> "Hoy"
        yesterday -> "Ayer"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale.forLanguageTag("es-ES")))
    }
}
