package datus.app.com.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")
        
        when (intent.action) {
            ACTION_RECORDATORIO -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Recordatorio"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Tu recordatorio diario"
                val hour = intent.getIntExtra(EXTRA_HOUR, 8)
                val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
                
                Log.d(TAG, "Mostrando recordatorio: $title - $message")
                
                // Mostrar la notificación
                NotificationHelper.showRecordatorioNotification(context, title, message)
                
                // Reprogramar para el día siguiente
                NotificationScheduler.scheduleDailyNotification(
                    context = context,
                    hour = hour,
                    minute = minute,
                    title = title,
                    message = message,
                    requestCode = 100
                )
            }
            ACTION_TASA_UPDATE -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Tasas Actualizadas"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Las tasas de cambio se han actualizado"
                NotificationHelper.showTasaNotification(context, title, message)
            }
            ACTION_ALERTA_PRECIO -> {
                val currency = intent.getStringExtra(EXTRA_CURRENCY) ?: "USD"
                val rate = intent.getDoubleExtra(EXTRA_RATE, 0.0)
                val direction = intent.getStringExtra(EXTRA_DIRECTION) ?: "up"
                NotificationHelper.showAlertaPrecioNotification(context, currency, rate, direction)
            }
        }
    }
    
    companion object {
        private const val TAG = "NotificationReceiver"
        
        const val ACTION_TASA_UPDATE = "datus.app.com.ACTION_TASA_UPDATE"
        const val ACTION_ALERTA_PRECIO = "datus.app.com.ACTION_ALERTA_PRECIO"
        const val ACTION_RECORDATORIO = "datus.app.com.ACTION_RECORDATORIO"
        
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_CURRENCY = "extra_currency"
        const val EXTRA_RATE = "extra_rate"
        const val EXTRA_DIRECTION = "extra_direction"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
    }
}
