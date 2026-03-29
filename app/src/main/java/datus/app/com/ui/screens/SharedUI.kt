@file:OptIn(ExperimentalMaterial3Api::class)
package datus.app.com.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import datus.app.com.NavRoutes
import datus.app.com.ui.notifications.NotificationsViewModel

import datus.app.com.utils.playClickSound

@Composable
fun DatusTopAppBar(
    title: String,
    navController: NavHostController,
    navigationIcon: @Composable (() -> Unit)? = null,
    showMenuIcon: Boolean = true,
    notificationsViewModel: NotificationsViewModel = hiltViewModel(),
    actions: @Composable RowScope.() -> Unit = {},
    canNavigateBack: Boolean = false
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showMenu by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (navigationIcon != null) {
            navigationIcon()
        } else {
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = title, 
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
        Row {
            actions()
            val unreadCount by notificationsViewModel.unreadCount.collectAsStateWithLifecycle()
            IconButton(onClick = {
                playClickSound(view)
                navController.navigate(NavRoutes.NOTIFICATIONS)
            }) {
                if (unreadCount > 0) {
                    BadgedBox(badge = { Badge { Text(unreadCount.toString()) } }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones")
                    }
                } else {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones")
                }
            }
            if (showMenuIcon) {
                Box {
                    IconButton(onClick = {
                        playClickSound(view)
                        showMenu = true
                    }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Configuración") },
                            leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                playClickSound(view)
                                showMenu = false
                                navController.navigate(NavRoutes.SETTINGS)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Compartir App") },
                            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                playClickSound(view)
                                showMenu = false
                                val mensaje = "Descarga la app Datus desde: https://datus.netlify.app/"
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.type = "text/plain"
                                intent.putExtra(Intent.EXTRA_TEXT, mensaje)
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Datus - Descarga la app")
                                context.startActivity(Intent.createChooser(intent, "Compartir Datus App"))
                            }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp)) // Ocupa el espacio del ícono de menú
            }
        }
    }


}

@Composable
fun ConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String,
    message: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = { 
            Text(
                title, 
                style = MaterialTheme.typography.headlineSmall 
            ) 
        },
        text = { 
            Text(
                message, 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(50)
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(50)
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Configuración") },
        text = { Text("Aquí podrás cambiar el tema y la pantalla de inicio. (Funcionalidad en desarrollo)") },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp) // Rectangular with rounded corners
                ) { 
                    Text("Cerrar") 
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}



