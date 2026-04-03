@file:OptIn(ExperimentalMaterial3Api::class)
package datus.app.com.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import datus.app.com.NavRoutes
import datus.app.com.R
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.ui.theme.ThemeViewModelFactory
import datus.app.com.ui.theme.ThemeOption
import datus.app.com.utils.playClickSound

@Composable
fun SettingsScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(LocalContext.current)),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by themeViewModel.theme.collectAsStateWithLifecycle()
    val view = LocalView.current
    val context = LocalContext.current
    var showContactDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = { DatusTopAppBar(
            title = "Configuración",
            navController = navController,
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Atrás") } },
            showMenuIcon = false,
        ) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sección de Notificaciones
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(NavRoutes.NOTIFICATION_SETTINGS) },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
                    }
                    Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            // Sección de Tema dentro de un Card centrado
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.NightsStay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tema", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ThemeIconOption(
                                Icons.Outlined.WbSunny,
                                description = "Claro",
                                selected = currentTheme == ThemeOption.LIGHT,
                                onClick = { themeViewModel.setTheme(ThemeOption.LIGHT) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ThemeIconOption(
                                Icons.Outlined.NightsStay,
                                description = "Oscuro",
                                selected = currentTheme == ThemeOption.DARK,
                                onClick = { themeViewModel.setTheme(ThemeOption.DARK) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ThemeIconOption(
                                painter = painterResource(id = R.drawable.auto_mode_24px),
                                description = "Automático",
                                selected = currentTheme == ThemeOption.AUTO,
                                onClick = { themeViewModel.setTheme(ThemeOption.AUTO) }
                            )
                        }
                    }
                }
            }

            // Sección de Actualizaciones (Web)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            playClickSound(view)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(datus.app.com.BuildConfig.DOWNLOAD_URL))
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizaciones", style = MaterialTheme.typography.titleMedium)
                    }
                    Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            // Sección de Soporte por WhatsApp
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            playClickSound(view)
                            val numero = "+5359072053"
                            val mensaje = "Hola, necesito ayuda con Datus App."
                            val url = "https://wa.me/$numero?text=" + Uri.encode(mensaje)
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(url)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No tienes WhatsApp instalado.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Whatsapp, contentDescription = null, tint = Color(0xFF25D366))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Soporte por WhatsApp", style = MaterialTheme.typography.titleMedium)
                    }
                    Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Versión ${datus.app.com.BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ThemeIconOption(
    imageVector: ImageVector? = null,
    painter: Painter? = null,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconTint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val view = LocalView.current
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.5f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
        onClick = {
            playClickSound(view)
            onClick()
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (imageVector != null) {
                Icon(imageVector, contentDescription = description, tint = iconTint, modifier = Modifier.fillMaxSize(0.5f))
            } else if (painter != null) {
                Icon(painter, contentDescription = description, tint = iconTint, modifier = Modifier.fillMaxSize(0.5f))
            }
        }
    }
}


