@file:OptIn(ExperimentalMaterial3Api::class)
package datus.app.com.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import dagger.hilt.android.lifecycle.HiltViewModel
import datus.app.com.NavRoutes
import datus.app.com.services.NautaAuthService
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.utils.isConnectedToNautaWifi
import datus.app.com.utils.playClickSound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import javax.inject.Inject

data class NautaLoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val username: String = "",
    val timeUsed: String = "",
    val timeAvailable: String = "",
    val rememberMe: Boolean = false,
    val savedUsername: String = ""
)

@HiltViewModel
class NautaLoginViewModel @Inject constructor(
    private val authService: NautaAuthService,
    private val dataStoreManager: datus.app.com.data.local.DataStoreManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NautaLoginUiState())
    val uiState: StateFlow<NautaLoginUiState> = _uiState.asStateFlow()
    
    init {
        loadSavedCredentials()
    }
    
    private fun loadSavedCredentials() {
        viewModelScope.launch {
            val savedUsername = dataStoreManager.loadNautaUsername() ?: ""
            val rememberMe = dataStoreManager.loadNautaRememberMe()
            _uiState.value = _uiState.value.copy(
                savedUsername = savedUsername,
                rememberMe = rememberMe
            )
        }
    }
    
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Por favor, complete todos los campos")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            
            val normalizedUsername = if (!username.contains("@")) {
                "$username@nauta.com.cu"
            } else {
                username.lowercase()
            }
            
            if (_uiState.value.rememberMe) {
                dataStoreManager.saveNautaUsername(normalizedUsername)
            }
            
            val result = authService.login(normalizedUsername, password)
            
            result.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        successMessage = response.message,
                        username = response.username,
                        timeUsed = response.timeUsed ?: "00:00:00",
                        timeAvailable = response.timeAvailable ?: "00:00:00"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error desconocido"
                    )
                }
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authService.logout()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoggedIn = false,
                username = "",
                timeUsed = "",
                timeAvailable = "",
                successMessage = null
            )
        }
    }
    
    fun setRememberMe(remember: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(rememberMe = remember)
            dataStoreManager.saveNautaRememberMe(remember)
            if (!remember) {
                dataStoreManager.clearNautaCredentials()
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null, isLoggedIn = false)
    }
    
    fun refreshAccountInfo() {
        viewModelScope.launch {
            val result = authService.refreshAccountInfo()
            result?.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        timeUsed = response.timeUsed,
                        timeAvailable = response.timeAvailable
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Error al actualizar"
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NautaLoginScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val viewModel: NautaLoginViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.savedUsername, uiState.rememberMe) {
        if (uiState.rememberMe && uiState.savedUsername.isNotEmpty()) {
            username = uiState.savedUsername
        }
    }

    if (uiState.isLoggedIn) {
        LoggedInContent(
            username = uiState.username,
            timeUsed = uiState.timeUsed,
            timeAvailable = uiState.timeAvailable,
            onLogout = { viewModel.logout() },
            onRefresh = { viewModel.refreshAccountInfo() },
            isLoading = uiState.isLoading,
            onBack = { navController.popBackStack() },
            navController = navController
        )
    } else {
        LoginContent(
            username = username,
            password = password,
            passwordVisible = passwordVisible,
            rememberMe = uiState.rememberMe,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onUsernameChange = { username = it },
            onPasswordChange = { password = it },
            onPasswordVisibleChange = { passwordVisible = !passwordVisible },
            onRememberMeChange = { viewModel.setRememberMe(it) },
            onLogin = { viewModel.login(username, password) },
            onBack = { navController.popBackStack() },
            focusManager = focusManager,
            navController = navController
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NautaTopAppBar(
    navController: NavHostController,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text("Nauta Hogar")
        },
        actions = {
            // Botón 1: Configuración de Nauta (auto-connect, etc.)
            IconButton(onClick = {
                playClickSound(view)
                navController.navigate(NavRoutes.NAUTA_SETTINGS)
            }) {
                Icon(Icons.Outlined.Settings, contentDescription = "Configuración Nauta")
            }
            // Botón 2: Notificaciones
            IconButton(onClick = {
                playClickSound(view)
                navController.navigate(NavRoutes.NOTIFICATIONS)
            }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones")
            }
            // Botón 3: Menú (3 puntitos)
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
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            playClickSound(view)
                            showMenu = false
                            navController.navigate(NavRoutes.SETTINGS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Compartir App") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
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
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun LoginContent(
    username: String,
    password: String,
    passwordVisible: Boolean,
    rememberMe: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibleChange: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    navController: NavHostController
) {
    val context = LocalContext.current
    var isConnectedToNauta by remember { mutableStateOf(false) }
    
    // Detectar estado de la conexión WiFi con portal cautivo ETECSA
    LaunchedEffect(Unit) {
        isConnectedToNauta = isConnectedToNautaWifi(context)
    }
    
    // Color del ícono: verde si está conectado a Nauta, rojo si no
    val wifiIconColor = if (isConnectedToNauta) {
        Color(0xFF4CAF50) // Verde
    } else {
        Color(0xFFF44336) // Rojo
    }
    
    Scaffold(
        topBar = {
            NautaTopAppBar(
                navController = navController,
                onNavigateToSettings = { navController.navigate(NavRoutes.NAUTA_SETTINGS) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Wifi,
                contentDescription = if (isConnectedToNauta) "Conectado a Nauta" else "No conectado a Nauta",
                modifier = Modifier.size(72.dp),
                tint = wifiIconColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Nauta Hogar",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Conecta a internet automáticamente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = username,
                onValueChange = { onUsernameChange(it.lowercase()) },
                label = { Text("Usuario") },
                placeholder = { Text("usuario@nauta.com.cu") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text("Formato: usuario@nauta.com.cu")
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { onPasswordChange(it) },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (username.isNotBlank() && password.isNotBlank()) {
                            onLogin()
                        }
                    }
                ),
                trailingIcon = {
                    TextButton(onClick = onPasswordVisibleChange) {
                        Text(if (passwordVisible) "Ocultar" else "Mostrar")
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = onRememberMeChange,
                    enabled = !isLoading
                )
                Text(
                    text = "Recordar usuario",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        return@Button
                    }
                    onLogin()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Conectando...")
                } else {
                    Text("Conectar", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Asegúrese de estar conectado a una red con portal cautivo ETECSA antes de iniciar sesión.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LoggedInContent(
    username: String,
    timeUsed: String,
    timeAvailable: String,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    onBack: () -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    var isConnectedToNauta by remember { mutableStateOf(false) }
    
    // Detectar estado de la conexión WiFi con portal cautivo ETECSA
    LaunchedEffect(Unit) {
        isConnectedToNauta = isConnectedToNautaWifi(context)
    }
    
    // Color del ícono: verde si está conectado a Nauta, rojo si no
    val wifiIconColor = if (isConnectedToNauta) {
        Color(0xFF4CAF50) // Verde
    } else {
        Color(0xFFF44336) // Rojo
    }
    
    Scaffold(
        topBar = {
            NautaTopAppBar(
                navController = navController,
                onNavigateToSettings = { navController.navigate(NavRoutes.NAUTA_SETTINGS) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Wifi,
                contentDescription = if (isConnectedToNauta) "Conectado a Nauta" else "No conectado a Nauta",
                modifier = Modifier.size(72.dp),
                tint = wifiIconColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Conectado",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Usuario",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = username,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tiempo consumido",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = timeUsed,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tiempo restante",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = timeAvailable,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRefresh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actualizar")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desconectando...")
                } else {
                    Icon(Icons.Outlined.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desconectar", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
