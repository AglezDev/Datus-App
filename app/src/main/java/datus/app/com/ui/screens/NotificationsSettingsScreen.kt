@file:OptIn(ExperimentalMaterial3Api::class)
package datus.app.com.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import datus.app.com.utils.NotificationScheduler
import datus.app.com.utils.playClickSound
import java.util.Calendar

@Composable
fun NotificationsSettingsScreen(
    navController: NavHostController,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    
    val recordatorioEnabled by viewModel.recordatorioEnabled.collectAsState()
    val recordatorioHour by viewModel.recordatorioHour.collectAsState()
    val recordatorioMinute by viewModel.recordatorioMinute.collectAsState()
    val notificacionesAlIniciarEnabled by viewModel.notificacionesAlIniciarEnabled.collectAsState()
    
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    
    // Verificar si las notificaciones están habilitadas a nivel del sistema
    var notificationsEnabled by remember {
        mutableStateOf(
            try {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            } catch (e: Exception) {
                false
            }
        )
    }
    
    // Verificar permiso de alarma exacta (Android 12+)
    var hasExactAlarmPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.canScheduleExactAlarms()
            } else true
        )
    }
    
    var showTimePicker by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        // Verificar si las notificaciones están realmente habilitadas
        notificationsEnabled = try {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } catch (e: Exception) {
            false
        }
    }
    
    // Launcher para permiso de alarma exacta
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            hasExactAlarmPermission = alarmManager.canScheduleExactAlarms()
        }
    }
    
    Scaffold(
        topBar = {
            DatusTopAppBar(
                title = "Notificaciones",
                navController = navController,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                showMenuIcon = false
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Aviso importante si notificaciones deshabilitadas
            if (!notificationsEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ Notificaciones deshabilitadas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tocá aquí para habilitar las notificaciones del sistema",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Permiso de notificaciones
            NotificationSettingCard(
                title = "Permiso de notificaciones",
                subtitle = if (hasNotificationPermission) "Activado" else "Necesario para recibir notificaciones",
                isChecked = hasNotificationPermission,
                onCheckedChange = { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
            
            // Permiso de alarma exacta (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                exactAlarmLauncher.launch(intent)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permiso de alarma exacta",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Necesario para el recordatorio diario",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.rotate(180f)
                        )
                    }
                }
            }
            
            HorizontalDivider()
            
            // Notificaciones al iniciar
            NotificationSettingCard(
                title = "Notificaciones al iniciar",
                subtitle = "Mostrar notificaciones cuando abras la app",
                isChecked = notificacionesAlIniciarEnabled,
                onCheckedChange = { enabled ->
                    playClickSound(view)
                    viewModel.setNotificacionesAlIniciar(enabled)
                }
            )
            
            HorizontalDivider()
            
            // Recordatorio diario con selector de hora
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recordatorio diario",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Recibir un recordatorio a una hora específica",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = recordatorioEnabled,
                            onCheckedChange = { enabled ->
                                playClickSound(view)
                                viewModel.setRecordatorio(enabled)
                                if (enabled) {
                                    // Verificar permiso de alarma exacta
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        if (!alarmManager.canScheduleExactAlarms()) {
                                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                            exactAlarmLauncher.launch(intent)
                                            viewModel.setRecordatorio(false)
                                            return@Switch
                                        }
                                    }
                                    NotificationScheduler.scheduleDailyNotification(
                                        context = context,
                                        hour = recordatorioHour,
                                        minute = recordatorioMinute,
                                        title = "Datus",
                                        message = "Consulta las tasas de cambio hoy",
                                        requestCode = 100
                                    )
                                } else {
                                    NotificationScheduler.cancelNotification(context, 100)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    
                    if (recordatorioEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTimePicker = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.rotate(180f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hora: ${"%02d".format(recordatorioHour)}:${"%02d".format(recordatorioMinute)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Las notificaciones se generan localmente sin necesidad de internet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = recordatorioHour,
            initialMinute = recordatorioMinute,
            is24Hour = true
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Seleccionar hora") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setRecordatorioHour(timePickerState.hour)
                        viewModel.setRecordatorioMinute(timePickerState.minute)
                        if (recordatorioEnabled) {
                            NotificationScheduler.scheduleDailyNotification(
                                context = context,
                                hour = timePickerState.hour,
                                minute = timePickerState.minute,
                                title = "Datus",
                                message = "Consulta las tasas de cambio hoy",
                                requestCode = 100
                            )
                        }
                        showTimePicker = false
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun NotificationSettingCard(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
