@file:OptIn(ExperimentalMaterial3Api::class)
package datus.app.com.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import dagger.hilt.android.lifecycle.HiltViewModel
import datus.app.com.data.local.DataStoreManager
import datus.app.com.data.local.NautaUser
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.utils.playClickSound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NautaSettingsUiState(
    val autoConnect: Boolean = false,
    val rememberWifi: Boolean = false,
    val reconnect: Boolean = false,
    val users: List<NautaUser> = emptyList(),
    val selectedUser: String? = null
)

@HiltViewModel
class NautaSettingsViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NautaSettingsUiState())
    val uiState: StateFlow<NautaSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val autoConnect = dataStoreManager.loadNautaAutoConnect()
            val rememberWifi = dataStoreManager.loadNautaRememberWifi()
            val reconnect = dataStoreManager.loadNautaReconnect()
            val users = dataStoreManager.loadNautaUsers()
            val selectedUser = users.find { it.isSelected }?.username

            _uiState.value = _uiState.value.copy(
                autoConnect = autoConnect,
                rememberWifi = rememberWifi,
                reconnect = reconnect,
                users = users,
                selectedUser = selectedUser
            )
        }
    }

    fun toggleAutoConnect(value: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveNautaAutoConnect(value)
            _uiState.value = _uiState.value.copy(autoConnect = value)
        }
    }

    fun toggleRememberWifi(value: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveNautaRememberWifi(value)
            _uiState.value = _uiState.value.copy(rememberWifi = value)
        }
    }

    fun toggleReconnect(value: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveNautaReconnect(value)
            _uiState.value = _uiState.value.copy(reconnect = value)
        }
    }

    fun addUser(username: String, password: String) {
        viewModelScope.launch {
            val newUser = NautaUser(username = username, password = password, isSelected = _uiState.value.users.isEmpty())
            dataStoreManager.addNautaUser(newUser)
            loadSettings()
        }
    }

    fun removeUser(username: String) {
        viewModelScope.launch {
            dataStoreManager.removeNautaUser(username)
            loadSettings()
        }
    }

    fun selectUser(username: String) {
        viewModelScope.launch {
            val updatedUsers = _uiState.value.users.map {
                it.copy(isSelected = it.username == username)
            }
            dataStoreManager.saveNautaUsers(updatedUsers)
            loadSettings()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NautaSettingsScreen(
    navController: NavHostController
) {
    val viewModel: NautaSettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current

    var showAddUserDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            DatusTopAppBar(
                title = "Configuración Nauta",
                navController = navController,
                canNavigateBack = true,
                showMenuIcon = false
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección: Opciones de Conexión
            item {
                Text(
                    text = "Opciones de Conexión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Autoconectar
            item {
                SettingsCard(
                    icon = Icons.Outlined.AutoFixHigh,
                    title = "Autoconectar",
                    description = "Conectar automáticamente al abrir la app"
                ) {
                    Switch(
                        checked = uiState.autoConnect,
                        onCheckedChange = { viewModel.toggleAutoConnect(it) }
                    )
                }
            }

            // Recordar WiFi
            item {
                SettingsCard(
                    icon = Icons.Outlined.Wifi,
                    title = "Recordar red WiFi",
                    description = "Recordar la red WiFi actual para autoconectar"
                ) {
                    Switch(
                        checked = uiState.rememberWifi,
                        onCheckedChange = { viewModel.toggleRememberWifi(it) }
                    )
                }
            }

            // Reconectar
            item {
                SettingsCard(
                    icon = Icons.Outlined.Refresh,
                    title = "Reconectar",
                    description = "Reconectar automáticamente si se cae la conexión"
                ) {
                    Switch(
                        checked = uiState.reconnect,
                        onCheckedChange = { viewModel.toggleReconnect(it) }
                    )
                }
            }

            // Sección: Usuarios Guardados
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Usuarios Guardados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = {
                        playClickSound(view)
                        showAddUserDialog = true
                    }) {
                        Icon(
                            Icons.Outlined.AddCircle,
                            contentDescription = "Agregar usuario",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Lista de usuarios
            items(uiState.users) { user ->
                UserItem(
                    user = user,
                    isSelected = user.username == uiState.selectedUser,
                    onSelect = { viewModel.selectUser(user.username) },
                    onDelete = {
                        viewModel.removeUser(user.username)
                        Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (uiState.users.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.PersonOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "No hay usuarios guardados. Agrega uno para comenzar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para agregar usuario
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = {
                showAddUserDialog = false
                newUsername = ""
                newPassword = ""
            },
            onConfirm = {
                if (newUsername.isNotBlank() && newPassword.isNotBlank()) {
                    viewModel.addUser(newUsername, newPassword)
                    Toast.makeText(context, "Usuario agregado", Toast.LENGTH_SHORT).show()
                    showAddUserDialog = false
                    newUsername = ""
                    newPassword = ""
                } else {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            username = newUsername,
            password = newPassword,
            onUsernameChange = { newUsername = it },
            onPasswordChange = { newPassword = it }
        )
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    trailingContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            trailingContent()
        }
    }
}

@Composable
private fun UserItem(
    user: NautaUser,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = null,
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isSelected) "Usuario activo" else "Toca para seleccionar",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                Icons.Outlined.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Agregar Usuario Nauta",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text("Usuario") },
                    placeholder = { Text("usuario@nauta.com.cu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null)
                    }
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        androidx.compose.ui.text.input.VisualTransformation.None
                    else
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "Ocultar" else "Mostrar")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(50)
            ) {
                Text("Guardar")
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
