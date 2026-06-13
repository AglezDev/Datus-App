@file:OptIn(ExperimentalMaterial3Api::class)
package datus.app.com.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import datus.app.com.NavRoutes
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.ui.components.DatusCard
import datus.app.com.ui.components.ModernIcon
import datus.app.com.utils.dialUssdCode
import datus.app.com.utils.playClickSound
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.min
import android.graphics.Bitmap
import androidx.compose.ui.res.painterResource
import android.graphics.Color as AndroidColor
import datus.app.com.R
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import datus.app.com.PortraitCaptureActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.produceState

private suspend fun generateQrBitmapAsync(text: String, size: Int = 512): Bitmap? {
    return withContext(Dispatchers.Default) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE
                }
            }
            Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
private fun QrBitmapState(text: String, size: Int = 512): Bitmap? {
    return produceState<Bitmap?>(initialValue = null, text, size) {
        value = generateQrBitmapAsync(text, size)
    }.value
}

// Enum para identificar el campo de destino del número de contacto
private enum class ContactTarget {
    TRANSFER, PRIVATE_CALL, REVERSE_CALL, NONE
}

@Composable
fun UtilitiesScreen(
    navController: NavHostController,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val view = LocalView.current
    val primaryColor = MaterialTheme.colorScheme.primary

    // --- Estados para los inputs ---
    var transferPhone by remember { mutableStateOf("") }
    var transferPin by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var privateCallNumber by remember { mutableStateOf("") }
    var reverseCallNumber by remember { mutableStateOf("") }

    // --- Estados de error ---
    var transferError by remember { mutableStateOf(false) }
    var privateCallError by remember { mutableStateOf(false) }
    var reverseCallError by remember { mutableStateOf(false) }

    // --- Lógica de selección de contactos robusta ---
    var contactPickerTarget by remember { mutableStateOf(ContactTarget.NONE) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri ->
            if (contactUri == null) {
                contactPickerTarget = ContactTarget.NONE
                return@rememberLauncherForActivityResult
            }
            try {
                val number = getPhoneNumberFromUri(context, contactUri)
                if (number != null) {
                    val formattedNumber = number.filter { it.isDigit() }.takeLast(8)
                    when (contactPickerTarget) {
                        ContactTarget.TRANSFER -> transferPhone = formattedNumber
                        ContactTarget.PRIVATE_CALL -> privateCallNumber = formattedNumber
                        ContactTarget.REVERSE_CALL -> reverseCallNumber = formattedNumber
                        ContactTarget.NONE -> { /* No hacer nada */ }
                    }
                } else {
                    Toast.makeText(context, "El contacto no tiene número de teléfono", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al leer el contacto", Toast.LENGTH_SHORT).show()
            } finally {
                contactPickerTarget = ContactTarget.NONE // Resetear el target
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permiso para leer contactos denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val launchContactPickerWithTarget: (ContactTarget) -> Unit = { target ->
        contactPickerTarget = target
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)) {
            PackageManager.PERMISSION_GRANTED -> contactPickerLauncher.launch(null)
            else -> permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // --- Fin de la lógica de contactos ---

    var showBankQRDialog by remember { mutableStateOf(false) }
    var showReceiveQRDialog by remember { mutableStateOf(false) }

        val phoneNumber by themeViewModel.phoneNumber.collectAsStateWithLifecycle()
    val bankCardNumber by themeViewModel.bankCardNumber.collectAsStateWithLifecycle()
    val transferPinFromSettings by themeViewModel.transferPin.collectAsStateWithLifecycle()

    LaunchedEffect(transferPinFromSettings) {
        if (transferPinFromSettings.isNotEmpty()) {
            transferPin = transferPinFromSettings
        }
    }

    val pinFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    val qrScannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            try {
                val qrData = result.contents.split(",").map { it.trim() }
                if (qrData.size >= 3 && qrData[0] == "DATUS" && qrData[1] == "RECIBIR") {
                    transferPhone = qrData[2]
                    if (qrData.size > 3) {
                        transferAmount = qrData[3]
                    }
                    coroutineScope.launch {
                        pinFocusRequester.requestFocus()
                    }
                } else {
                    Toast.makeText(context, "Código QR no válido", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al procesar el código QR", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = { DatusTopAppBar(title = "Útiles", navController = navController,) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SmallActionCard(
                        modifier = Modifier.weight(1f),
                        painter = painterResource(id = R.drawable.enviar_24px),
                        title = "Enviar",
                        description = "Pídale al destinatario el QR desde el botón Recibir en Datus App",
                        onAction = { 
                            playClickSound(view)
                            val options = ScanOptions()
                            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            options.setPrompt("Pídale al destinatario el QR desde el botón Recibir en Datus App")
                            options.setCameraId(0)
                            options.setBeepEnabled(true)
                            options.setBarcodeImageEnabled(true)
                            options.setOrientationLocked(false)
                            options.setCaptureActivity(PortraitCaptureActivity::class.java)
                            qrScannerLauncher.launch(options)
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    SmallActionCard(
                        modifier = Modifier.weight(1f),
                        painter = painterResource(id = R.drawable.recibir_24px),
                        title = "Recibir",
                        description = "Pídale al emisor que escanee su código QR con Datus App la opción Enviar",
                        onAction = { 
                            if (phoneNumber.isBlank()) {
                                Toast.makeText(context, "Por favor rellene primero estos campos.", Toast.LENGTH_LONG).show()
                                navController.navigate("${NavRoutes.USER_SETTINGS}?focusPhoneNumber=true") {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId)
                                }
                            } else {
                                playClickSound(view)
                                showReceiveQRDialog = true
                            }
                        }
                    )
                }
            }

            // Card para Transferir Saldo
            item {
                UtilityActionCard(
                    icon = Icons.Outlined.SwapHoriz,
                    title = "Transferir Saldo",
                    description = "Transfiere saldo a otro usuario.",
                    fields = listOf(
                        {
                            OutlinedTextField(
                                value = transferPhone,
                                onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) transferPhone = it },
                                label = { Text("Teléfono") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                isError = transferError && (transferPhone.length !in 1..8),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                trailingIcon = {
                                    IconButton(onClick = { launchContactPickerWithTarget(ContactTarget.TRANSFER) }) {
                                        Icon(Icons.Outlined.Contacts, "Seleccionar Contacto", tint = primaryColor)
                                    }
                                }
                            )
                        },
                        {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = transferPin,
                                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) transferPin = it },
                                    label = { Text("PIN") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    isError = transferError && (transferPin.length !in 1..4),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(pinFocusRequester),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = transferAmount,
                                    onValueChange = { transferAmount = it.filter { char -> char.isDigit() } },
                                    label = { Text("Monto") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    isError = transferError && transferAmount.isBlank(),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    ),
                    onAction = {
                        playClickSound(view)
                        transferError = true
                        if (transferPhone.length in 1..8 && transferPin.length in 1..4 && transferAmount.isNotBlank()) {
                            val amountAsInt = transferAmount.toIntOrNull() ?: 0
                            val ussd = "*234*1*${transferPhone}*${transferPin}*${amountAsInt}#"
                            dialUssdCode(context, ussd)
                            transferPhone = ""; transferPin = ""; transferAmount = ""; transferError = false
                        }
                    },
                    actionLabel = "Transferir",
                                                            actionEnabled = transferPhone.isNotBlank() && transferPin.isNotBlank() && transferAmount.isNotBlank()
                )
            }
            // Card para Llamada Privada
            item {
                DatusCard(
                    onClick = {
                        playClickSound(view)
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:*31#")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ModernIcon(
                            imageVector = Icons.Outlined.PhoneLocked,
                            contentDescription = "Llamada Privada",
                            containerSize = 48.dp,
                            iconSize = 28.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Llamada Privada",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Oculta tu número al llamar (Abre marcador)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Card para Llamada con Cobro Revertido
            item {
                DatusCard(
                    onClick = {
                        playClickSound(view)
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:*99")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ModernIcon(
                            imageVector = Icons.AutoMirrored.Outlined.CallReceived,
                            contentDescription = "Llamada con Cobro Revertido",
                            containerSize = 48.dp,
                            iconSize = 28.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Llamada con Cobro Revertido",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "El destinatario paga la llamada (Abre marcador)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showBankQRDialog) {
        BankTransferQRDialog(
            onDismiss = { showBankQRDialog = false },
            phoneNumber = phoneNumber,
            bankCardNumber = bankCardNumber
        )
    }

    if (showReceiveQRDialog) {
        ReceiveQRDialog(
            onDismiss = { showReceiveQRDialog = false },
            phoneNumber = phoneNumber
        )
    }
}

@Composable
fun ReceiveQRDialog(
    onDismiss: () -> Unit,
    phoneNumber: String
) {
    var amount by remember { mutableStateOf("") }
    var qrCodeText by remember { mutableStateOf("DATUS,RECIBIR,$phoneNumber,") }
    var isGenerating by remember { mutableStateOf(false) }
    val customTextFieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    )

    LaunchedEffect(amount) {
        isGenerating = true
        qrCodeText = if (amount.isNotBlank()) {
            "DATUS,RECIBIR,$phoneNumber,$amount"
        } else {
            "DATUS,RECIBIR,$phoneNumber,"
        }
        isGenerating = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Recibir Saldo") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isGenerating) {
                    CircularProgressIndicator()
                } else {
                    val bitmap = QrBitmapState(qrCodeText)

                    bitmap?.let { btm ->
                    Image(bitmap = btm.asImageBitmap(), contentDescription = "Código QR", modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = customTextFieldColors
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Cerrar")
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

private fun getPhoneNumberFromUri(context: Context, uri: Uri): String? {
    var phoneNumber: String? = null
    val contactId = uri.lastPathSegment

    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
    val selectionArgs = arrayOf(contactId)

    context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { phoneCursor ->
        if (phoneCursor.moveToFirst()) {
            val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (numberIndex != -1) {
                phoneNumber = phoneCursor.getString(numberIndex)
            }
        }
    }
    return phoneNumber
}

@Composable
private fun UtilityActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    fields: List<@Composable () -> Unit>,
    onAction: () -> Unit,
    actionLabel: String,
    actionEnabled: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val configuration = LocalConfiguration.current
    val adaptiveTitleSize = remember(configuration.screenWidthDp) { min(20f, configuration.screenWidthDp / 18f) }
    val adaptiveIconSize = remember(configuration.screenWidthDp) { min(40f, configuration.screenWidthDp / 9f) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ModernIcon(imageVector = icon, contentDescription = title, containerSize = 48.dp, iconSize = 28.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = adaptiveTitleSize.sp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }
            fields.forEach { field -> field() }
            Button(
                onClick = onAction, 
                modifier = Modifier.fillMaxWidth(), 
                enabled = actionEnabled,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(actionLabel)
            }
        }
    }
}

private fun isValidPrivateNumber(number: String): Boolean {
    val clean = number.filter { it.isDigit() }
    return when {
        clean.length == 8 -> true
        clean.length == 10 && clean.startsWith("05") -> true
        clean.length == 11 && clean.startsWith("53") -> true
        else -> false
    }
}

@Composable
private fun SmallActionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    onAction: () -> Unit,
    icon: ImageVector? = null,
    painter: Painter? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val adaptiveFontSize = remember(configuration.screenWidthDp) { min(20f, configuration.screenWidthDp / 18f) }
    val adaptiveIconSize = remember(configuration.screenWidthDp) { min(40f, configuration.screenWidthDp / 9f) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onAction()
        } else {
            Toast.makeText(context, "Permiso para realizar llamadas denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth().aspectRatio(1.5f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = {
            playClickSound(view)
            showDialog = true
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                ModernIcon(imageVector = icon, contentDescription = title, containerSize = 48.dp, iconSize = 28.dp)
            } else if (painter != null) {
                Icon(painter = painter, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(adaptiveIconSize.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = adaptiveFontSize.sp))
        }
    }

    if (showDialog) {
        val dialogTitle = if (title == "Enviar") "Enviar Saldo Movil" else if (title == "Recibir") "Recibir Saldo Movil" else title
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(dialogTitle) },
            text = { Text(description) },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            onAction()
                        } else {
                            launcher.launch(Manifest.permission.CALL_PHONE)
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun BankTransferQRDialog(
    onDismiss: () -> Unit,
    phoneNumber: String,
    bankCardNumber: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("QR de Transferencia Bancaria") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                val qrCodeText = "TRANSFERMOVIL_ETECSA,TRANSFERENCIA,${bankCardNumber},${phoneNumber},"
                val bitmap = QrBitmapState(qrCodeText)

                bitmap?.let { btm ->
                    Image(bitmap = btm.asImageBitmap(), contentDescription = "Código QR", modifier = Modifier.fillMaxWidth(0.8f).aspectRatio(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("El remitente debe escanear con Transfermovil")
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Cerrar")
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}