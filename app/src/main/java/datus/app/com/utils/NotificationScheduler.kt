package datus.app.com.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object NotificationScheduler {
    
    private const val TAG = "NotificationScheduler"
    
    /**
     * Programa una notificación diaria a una hora específica
     * Usa setExactAndAllowWhileIdle y el receiver se reprograma a sí mismo
     */
    fun scheduleDailyNotification(
        context: Context,
        hour: Int,
        minute: Int,
        title: String,
        message: String,
        requestCode: Int = 100
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_RECORDATORIO
            putExtra(NotificationReceiver.EXTRA_TITLE, title)
            putExtra(NotificationReceiver.EXTRA_MESSAGE, message)
            putExtra(NotificationReceiver.EXTRA_HOUR, hour)
            putExtra(NotificationReceiver.EXTRA_MINUTE, minute)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calcular la próxima ocurrencia
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // Si ya pasó hoy, programar para mañana
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        Log.d(TAG, "Programando recordatorio para: ${calendar.time}")
        
        // Usar setExactAndAllowWhileIdle para mayor precisión
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancela una notificación programada
     */
    fun cancelNotification(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
    
    /**
     * Programa una alerta de precio (una sola vez)
     */
    fun schedulePriceAlert(
        context: Context,
        currency: String,
        targetRate: Double,
        direction: String, // "up" o "down"
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_ALERTA_PRECIO
            putExtra(NotificationReceiver.EXTRA_CURRENCY, currency)
            putExtra(NotificationReceiver.EXTRA_RATE, targetRate)
            putExtra(NotificationReceiver.EXTRA_DIRECTION, direction)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Esta alerta se dispara cuando se cumplan las condiciones en el ViewModel
        // Por ahora solo guardamos la configuración
        // El ViewModel será quien verifique y dispare cuando corresponda
    }
    
    /**
     * Programa notificación de actualización de tasas
     */
    fun scheduleTasaUpdateNotification(
        context: Context,
        delayMinutes: Long = 0
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_TASA_UPDATE
            putExtra(NotificationReceiver.EXTRA_TITLE, "Tasas Actualizadas")
            putExtra(NotificationReceiver.EXTRA_MESSAGE, "Las tasas de cambio se han actualizado")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            200,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = if (delayMinutes > 0) {
            System.currentTimeMillis() + (delayMinutes * 60 * 1000)
        } else {
            System.currentTimeMillis() + (30 * 60 * 1000) // Default 30 min
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
