@file:OptIn(ExperimentalMaterial3Api::class)
package datus.app.com.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.automirrored.outlined.Logout
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
import datus.app.com.ui.components.DatusCard
import datus.app.com.ui.components.ModernIcon
import datus.app.com.ui.theme.Dimens
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.utils.isConnectedToNautaWifi
import datus.app.com.utils.playClickSound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import javax.inject.Inject

import androidx.compose.runtime.Immutable

@Immutable
data class NautaLoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val username: String = "",
    val timeUsed: String = "",
    val timeAvailable: String = "",
    val rememberMe: Boolean = false,
    val savedUsername: String = "",
    val savedPassword: String = ""
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
            val savedPassword = dataStoreManager.loadNautaPassword() ?: ""
            val rememberMe = dataStoreManager.loadNautaRememberMe()
            _uiState.value = _uiState.value.copy(
                savedUsername = savedUsername,
                savedPassword = savedPassword,
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
                dataStoreManager.saveNautaPassword(password)
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
                    silentRefresh()
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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authService.refreshAccountInfo()
            result?.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        timeUsed = response.timeUsed ?: "00:00:00",
                        timeAvailable = response.timeAvailable ?: "00:00:00"
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al actualizar"
                    )
                }
            )
        }
    }

    private fun silentRefresh() {
        viewModelScope.launch {
            val result = authService.refreshAccountInfo()
            result?.fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        timeUsed = response.timeUsed ?: "00:00:00",
                        timeAvailable = response.timeAvailable ?: "00:00:00"
                    )
                },
                onFailure = { }
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
    val context = LocalContext.current

    // Consolidated WiFi check - runs once at the parent level
    var isConnectedToNauta by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isConnectedToNauta = isConnectedToNautaWifi(context)
    }
    val wifiIconColor = if (isConnectedToNauta) Color(0xFF4CAF50) else Color(0xFFF44336)

    LaunchedEffect(uiState.savedUsername, uiState.savedPassword, uiState.rememberMe) {
        if (uiState.rememberMe) {
            username = uiState.savedUsername
            password = uiState.savedPassword
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
            navController = navController,
            isConnectedToNauta = isConnectedToNauta,
            wifiIconColor = wifiIconColor
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
            navController = navController,
            isConnectedToNauta = isConnectedToNauta,
            wifiIconColor = wifiIconColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NautaTopAppBar(
    navController: NavHostController,
) {
    val view = LocalView.current
    DatusTopAppBar(
        title = "Nauta Hogar",
        navController = navController,
        showMenuIcon = true,
        actions = {
            IconButton(onClick = {
                playClickSound(view)
                navController.navigate(NavRoutes.NAUTA_SETTINGS)
            }) {
                Icon(Icons.Outlined.Settings, contentDescription = "Configuración Nauta")
            }
        }
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
    navController: NavHostController,
    isConnectedToNauta: Boolean,
    wifiIconColor: Color
) {
    Scaffold(
        topBar = {
            NautaTopAppBar(navController = navController)
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
            ModernIcon(
                imageVector = Icons.Outlined.Wifi,
                contentDescription = if (isConnectedToNauta) "Conectado a Nauta" else "No conectado a Nauta",
                containerSize = 80.dp,
                iconSize = 40.dp,
                tint = wifiIconColor,
                containerColor = wifiIconColor.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nauta Hogar",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
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
                shape = RoundedCornerShape(Dimens.cardCorner),
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
                shape = RoundedCornerShape(Dimens.cardCorner),
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
                shape = RoundedCornerShape(Dimens.cardCorner),
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

            DatusCard(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
    navController: NavHostController,
    isConnectedToNauta: Boolean,
    wifiIconColor: Color
) {
    Scaffold(
        topBar = {
            NautaTopAppBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ModernIcon(
                imageVector = Icons.Outlined.Wifi,
                contentDescription = "Conectado",
                containerSize = 80.dp,
                iconSize = 40.dp,
                containerColor = if (isConnectedToNauta) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f),
                tint = if (isConnectedToNauta) Color(0xFF4CAF50) else Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(Dimens.md))

            Text(
                text = "Conectado",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Dimens.lg))

            DatusCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.md),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    TimeRow(
                        label = "Tiempo restante",
                        value = timeAvailable,
                        isPrimary = true
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    TimeRow(
                        label = "Tiempo consumido",
                        value = timeUsed,
                        isPrimary = false
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    UserRow(username = username)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.xl))

            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(Dimens.cardCorner)
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(Dimens.iconMedium))
                Spacer(modifier = Modifier.width(Dimens.sm))
                Text("Actualizar", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(Dimens.md))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(Dimens.cardCorner),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimens.iconMedium),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(Dimens.sm))
                    Text("Desconectando...")
                } else {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(Dimens.sm))
                    Text("Desconectar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TimeRow(label: String, value: String, isPrimary: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun UserRow(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Usuario",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = username,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
