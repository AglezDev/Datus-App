@file:OptIn(ExperimentalMaterial3Api::class)
package datus.app.com.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import datus.app.com.NavRoutes
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.ui.components.DatusCard
import datus.app.com.ui.components.ModernIcon
import datus.app.com.utils.dialUssdCode
import datus.app.com.utils.playClickSound

fun generateQrBitmap(text: String, size: Int = 512): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.TRANSPARENT)
        }
    }
    return bmp
}

@Composable
fun MenuScreen(navController: NavHostController, themeViewModel: ThemeViewModel) {

    val context = LocalContext.current
    val view = LocalView.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Más opciones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { 
                        playClickSound(view)
                        navController.popBackStack() 
                    }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(
                icon = Icons.Outlined.Settings,
                title = "Configuración",
                subtitle = "Personaliza la app",
                onClick = { 
                    playClickSound(view)
                    navController.navigate(NavRoutes.SETTINGS)
                }
            )

            MenuCard(
                icon = Icons.Outlined.Whatsapp,
                title = "Soporte",
                subtitle = "Escríbenos por WhatsApp",
                onClick = {
                    playClickSound(view)
                    val numero = "5359072053"
                    val mensaje = "Hola, necesito ayuda con la app Datus."
                    val url = "https://wa.me/$numero?text=" + Uri.encode(mensaje)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No tienes WhatsApp instalado.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            
            MenuCard(
                icon = Icons.Outlined.Share,
                title = "Compartir",
                subtitle = "Recomienda la app",
                onClick = {
                    playClickSound(view)
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
}

@Composable
fun MenuCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val view = LocalView.current
    DatusCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { 
            playClickSound(view)
            onClick() 
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModernIcon(
                imageVector = icon,
                contentDescription = title,
                containerSize = 40.dp,
                iconSize = 22.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle, 
                    style = MaterialTheme.typography.bodySmall,
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