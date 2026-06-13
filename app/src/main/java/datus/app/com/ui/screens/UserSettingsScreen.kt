package datus.app.com.ui.screens

import android.widget.Toast
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.min
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.ui.components.ModernIcon
import datus.app.com.utils.playClickSound
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(navController: NavHostController, themeViewModel: ThemeViewModel, focusPhoneNumber: Boolean) {
    val view = LocalView.current
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val configuration = LocalConfiguration.current
    val adaptiveBodySize = min(14f, configuration.screenWidthDp / 25f)
    val adaptiveTitleSize = min(16f, configuration.screenWidthDp / 22f)

    LaunchedEffect(focusPhoneNumber) {
        if (focusPhoneNumber) {
            focusRequester.requestFocus()
        }
    }
    Scaffold(
        topBar = {
            DatusTopAppBar(
                title = "Configuraciones de Usuario",
                navController = navController,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Atrás")
                    }
                },
                showMenuIcon = false,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Tus datos se guardan de forma segura solo en tu dispositivo.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = adaptiveBodySize.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                val phoneNumber by themeViewModel.phoneNumber.collectAsStateWithLifecycle()

                val isPhoneValid by remember { derivedStateOf { phoneNumber.length == 8 || phoneNumber.isBlank() } }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ModernIcon(Icons.Outlined.WbSunny, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Datos para operaciones", style = MaterialTheme.typography.titleMedium.copy(fontSize = adaptiveTitleSize.sp))
                        }
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 8) themeViewModel.savePhoneNumber(it.filter { char -> char.isDigit() }) },
                            label = { Text("Número de teléfono") },
                            leadingIcon = { Text("+53") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                IconButton(onClick = { themeViewModel.savePhoneNumber("") }) {
                                    Icon(Icons.Outlined.Delete, "Eliminar", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            ),
                            isError = !isPhoneValid,
                            supportingText = { 
                                if (!isPhoneValid) {
                                    Text("Debe tener 8 dígitos", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            val focusManager = LocalFocusManager.current
                            Button(
                                onClick = { 
                                    playClickSound(view)
                                    focusManager.clearFocus()
                                    Toast.makeText(context, "Datos guardados", Toast.LENGTH_SHORT).show()
                                },
                                enabled = phoneNumber.isBlank() || isPhoneValid,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Guardar")
                            }
                        }
                    }
                }
            }
            item {
                val transferPin by themeViewModel.transferPin.collectAsStateWithLifecycle()
                val isPinValid by remember { derivedStateOf { transferPin.length == 4 || transferPin.isBlank() } }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ModernIcon(Icons.Outlined.Password, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Definir PIN de transferencia", style = MaterialTheme.typography.titleMedium.copy(fontSize = adaptiveTitleSize.sp))
                        }
                        OutlinedTextField(
                            value = transferPin,
                            onValueChange = { if (it.length <= 4) themeViewModel.saveTransferPin(it.filter { char -> char.isDigit() }) },
                            label = { Text("PIN de 4 dígitos") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                IconButton(onClick = { themeViewModel.saveTransferPin("") }) {
                                    Icon(Icons.Outlined.Delete, "Eliminar", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            ),
                            isError = !isPinValid,
                            supportingText = { 
                                if (!isPinValid) {
                                    Text("Debe tener 4 dígitos", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            val focusManager = LocalFocusManager.current
                            Button(
                                onClick = {
                                    playClickSound(view)
                                    focusManager.clearFocus()
                                    Toast.makeText(context, "PIN guardado", Toast.LENGTH_SHORT).show()
                                },
                                enabled = transferPin.isBlank() || isPinValid,
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Guardar PIN")
                            }
                        }
                    }
                }
            }
            
        }
    }
}