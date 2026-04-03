package datus.app.com.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import datus.app.com.R
import java.lang.reflect.Method

class SaldoPrincipalWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_EXECUTE_SALDO = "datus.app.com.ACTION_EXECUTE_SALDO"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_EXECUTE_SALDO) {
            // Ejecutar USSD directamente usando reflexión
            executeUssdCode(context, "*222#")
        }
    }

    /**
     * Ejecuta un código USSD directamente usando reflexión
     * Este método intenta enviar el comando USSD sin abrir la interfaz de llamadas
     */
    private fun executeUssdCode(context: Context, ussdCode: String) {
        try {
            // Método 1: Usar reflexión para llamar al método sendUssdRequest de ITelephony
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
            if (telecomManager != null) {
                // Codificar correctamente el código USSD con # al final
                val encodedCode = android.net.Uri.encode("*222#")
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = android.net.Uri.parse("tel:$encodedCode")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
                return
            }
        } catch (e: Exception) {
            // Si falla, intentar método alternativo
        }
        
        try {
            // Método 2: Intento directo con ACTION_CALL - Código completo *222#
            val encodedCode = android.net.Uri.encode("*222#")
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = android.net.Uri.parse("tel:$encodedCode")
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            callIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            context.startActivity(callIntent)
        } catch (e: Exception) {
            // Método 3: Copiar al portapapeles como último recurso
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Código USSD", "*222#")
                clipboard.setPrimaryClip(clip)
                android.widget.Toast.makeText(
                    context, 
                    "Código *222# copiado al portapapeles", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } catch (e2: Exception) {
                // No hacer nada si todo falla
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_saldo_principal)
        
        // Set up click action to execute USSD code directly
        val intent = Intent(context, SaldoPrincipalWidgetProvider::class.java).apply {
            action = ACTION_EXECUTE_SALDO
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_saldo_container, pendingIntent)
        
        // Apply theme-aware icon tint
        val isNightMode = context.resources.configuration.uiMode and 
            android.content.res.Configuration.UI_MODE_NIGHT_MASK == 
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        
        val iconTint = if (isNightMode) {
            android.graphics.Color.parseColor("#64B5F6")
        } else {
            android.graphics.Color.parseColor("#1976D2")
        }
        views.setInt(R.id.widget_saldo_icon, "setColorFilter", iconTint)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
