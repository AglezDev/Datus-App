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
import datus.app.com.ui.components.ModernIcon
import datus.app.com.ui.theme.ThemeViewModelFactory
import datus.app.com.ui.theme.ThemeOption
import datus.app.com.ui.components.DatusCard
import datus.app.com.utils.playClickSound

@Composable
fun SettingsScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(LocalContext.current)),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by themeViewModel.theme.collectAsStateWithLifecycle()
    val currentDynamicColor by themeViewModel.useDynamicColor.collectAsStateWithLifecycle()
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
            DatusCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(NavRoutes.NOTIFICATION_SETTINGS) },
                shape = RoundedCornerShape(24.dp),
                elevation = 4.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ModernIcon(Icons.Outlined.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
                    }
                    Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            // Sección de Tema
            DatusCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 4.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ModernIcon(Icons.Outlined.NightsStay, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tema", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ThemeIconOption(
                            imageVector = Icons.Outlined.WbSunny,
                            description = "Claro",
                            selected = currentTheme == ThemeOption.LIGHT,
                            onClick = { themeViewModel.setTheme(ThemeOption.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeIconOption(
                            imageVector = Icons.Outlined.NightsStay,
                            description = "Oscuro",
                            selected = currentTheme == ThemeOption.DARK,
                            onClick = { themeViewModel.setTheme(ThemeOption.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeIconOption(
                            painter = painterResource(id = R.drawable.auto_mode_24px),
                            description = "Automático",
                            selected = currentTheme == ThemeOption.AUTO,
                            onClick = { themeViewModel.setTheme(ThemeOption.AUTO) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (currentTheme == ThemeOption.DARK || currentTheme == ThemeOption.AUTO) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ModernIcon(Icons.Outlined.DarkMode, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AMOLED true black", style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = currentTheme == ThemeOption.AMOLED,
                                onCheckedChange = { checked ->
                                    themeViewModel.setTheme(if (checked) ThemeOption.AMOLED else currentTheme)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ModernIcon(Icons.Outlined.Palette, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Color dinámico", style = MaterialTheme.typography.bodyMedium)
                                Text("Colores del sistema (Android 12+)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = currentDynamicColor,
                            onCheckedChange = { themeViewModel.setUseDynamicColor(it) }
                        )
                    }
                }
            }

            // Sección de Actualizaciones (Web)
            DatusCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    playClickSound(view)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(datus.app.com.BuildConfig.DOWNLOAD_URL))
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(24.dp),
                elevation = 4.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ModernIcon(Icons.Outlined.SystemUpdate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizaciones", style = MaterialTheme.typography.titleMedium)
                    }
                    Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }

            // Sección de Soporte por WhatsApp
            DatusCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
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
                },
                shape = RoundedCornerShape(24.dp),
                elevation = 4.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ModernIcon(Icons.Outlined.Whatsapp, contentDescription = null, containerColor = Color(0xFF25D366).copy(alpha = 0.15f), tint = Color(0xFF25D366))
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val view = LocalView.current
    DatusCard(
        modifier = modifier.aspectRatio(1.5f),
        onClick = {
            playClickSound(view)
            onClick()
        },
        shape = RoundedCornerShape(24.dp),
        elevation = 2.dp,
        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
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


