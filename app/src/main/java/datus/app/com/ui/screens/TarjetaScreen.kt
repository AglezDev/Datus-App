package datus.app.com.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import datus.app.com.ui.theme.Bank
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.ui.components.ModernIcon
import datus.app.com.utils.dialUssdCode
import datus.app.com.utils.playClickSound
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaScreen(navController: NavHostController, themeViewModel: ThemeViewModel) {
    val configuration = LocalConfiguration.current
    val adaptiveFontSize = min(16f, configuration.screenWidthDp / 22f)
    val adaptiveIconSize = min(32f, configuration.screenWidthDp / 11f)
    Scaffold(
        topBar = { DatusTopAppBar(title = "Tarjeta", navController = navController,) }
    ) { innerPadding ->
        val selectedBank by themeViewModel.selectedBank.collectAsStateWithLifecycle()
        val showBankSelectionDialog = remember { mutableStateOf(false) }
        val context = LocalContext.current
        val view = LocalView.current

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    val ussdCode = when (selectedBank) {
                        Bank.BANDEC -> "*444*40*02#"
                        Bank.BPA -> "*444*40*01#"
                        Bank.BANMET -> "*444*40*03#"
                        else -> ""
                    }
                    if (ussdCode.isNotEmpty()) {
                        dialUssdCode(context, ussdCode)
                    } else {
                        Toast.makeText(context, "Seleccione un banco en la configuración", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Permiso para realizar llamadas denegado", Toast.LENGTH_SHORT).show()
                }
            }
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clickable {
                            playClickSound(view)
                            showBankSelectionDialog.value = true
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ModernIcon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Autenticarse",
                            containerSize = 48.dp,
                            iconSize = 28.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Autenticarse", style = MaterialTheme.typography.titleLarge.copy(fontSize = adaptiveFontSize.sp), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clickable {
                            playClickSound(view)
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                dialUssdCode(context, "*444*70#")
                            } else {
                                launcher.launch(Manifest.permission.CALL_PHONE)
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ModernIcon(
                            imageVector = Icons.Outlined.PowerOff,
                            contentDescription = "Desconectar",
                            containerSize = 48.dp,
                            iconSize = 28.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Desconectar", style = MaterialTheme.typography.titleLarge.copy(fontSize = adaptiveFontSize.sp), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f)
                        .clickable {
                            playClickSound(view)
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                dialUssdCode(context, "*444*46#")
                            } else {
                                launcher.launch(Manifest.permission.CALL_PHONE)
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        ModernIcon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Consultar Saldo de Tarjeta",
                            containerSize = 48.dp,
                            iconSize = 28.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Consultar de Saldo de Tarjeta", style = MaterialTheme.typography.titleLarge.copy(fontSize = adaptiveFontSize.sp))
                    }
                }
            }
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f)
                        .clickable {
                            playClickSound(view)
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                dialUssdCode(context, "*444*54#")
                            } else {
                                launcher.launch(Manifest.permission.CALL_PHONE)
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        ModernIcon(
                            imageVector = Icons.Outlined.PhoneAndroid,
                            contentDescription = "Recargar Saldo Movil",
                            containerSize = 48.dp,
                            iconSize = 28.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Recargar Saldo Movil", style = MaterialTheme.typography.titleLarge.copy(fontSize = adaptiveFontSize.sp))
                    }
                }
            }
            
        }
        if (showBankSelectionDialog.value) {
            AlertDialog(
                onDismissRequest = { showBankSelectionDialog.value = false },
                title = { Text("Seleccione un Banco", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Elija un banco y luego escriba su pin de autenticación", textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                showBankSelectionDialog.value = false
                                playClickSound(view)
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                    dialUssdCode(context, "*444*40*02#")
                                } else {
                                    launcher.launch(Manifest.permission.CALL_PHONE)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp) // Rectangular with rounded corners
                        ) {
                            Text("BANDEC")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                showBankSelectionDialog.value = false
                                playClickSound(view)
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                    dialUssdCode(context, "*444*40*01#")
                                } else {
                                    launcher.launch(Manifest.permission.CALL_PHONE)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp) // Rectangular with rounded corners
                        ) {
                            Text("BPA")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                showBankSelectionDialog.value = false
                                playClickSound(view)
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                    dialUssdCode(context, "*444*40*03#")
                                } else {
                                    launcher.launch(Manifest.permission.CALL_PHONE)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp) // Rectangular with rounded corners
                        ) {
                            Text("BANMET")
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showBankSelectionDialog.value = false },
                            shape = RoundedCornerShape(8.dp) // Rectangular with rounded corners
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            )
        }
    }
}




