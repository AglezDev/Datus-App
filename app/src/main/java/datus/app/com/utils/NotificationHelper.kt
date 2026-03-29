package datus.app.com.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import datus.app.com.MainActivity
import datus.app.com.R

object NotificationHelper {
    
    const val CHANNEL_ID_TASAS = "tasas_notifications"
    const val CHANNEL_ID_ALERTAS = "alertas_notifications"
    const val CHANNEL_ID_RECORDATORIOS = "recordatorios_notifications"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para notificaciones de tasas
            val channelTasas = NotificationChannel(
                CHANNEL_ID_TASAS,
                "Tasas de Cambio",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando se actualizan las tasas de cambio"
            }
            
            // Canal para alertas de precio
            val channelAlertas = NotificationChannel(
                CHANNEL_ID_ALERTAS,
                "Alertas de Precio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas cuando las tasas alcanzan un valor específico"
            }
            
            // Canal para recordatorios
            val channelRecordatorios = NotificationChannel(
                CHANNEL_ID_RECORDATORIOS,
                "Recordatorios Diarios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios diarios programados"
            }
            
            notificationManager.createNotificationChannels(
                listOf(channelTasas, channelAlertas, channelRecordatorios)
            )
        }
    }

    private fun getAppIcon(context: Context): Bitmap? {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val drawable = packageManager.getApplicationIcon(appInfo)
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    fun showTasaNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASAS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Permiso no concedido
        }
    }
    
    fun showAlertaPrecioNotification(context: Context, currency: String, rate: Double, direction: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, currency.hashCode(), intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val directionEmoji = if (direction == "up") "📈" else "📉"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTAS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Alerta de Precio $directionEmoji")
            .setContentText("1 $currency = ${"%.2f".format(rate)} CUP")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(currency.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permiso no concedido
        }
    }
    
    fun showRecordatorioNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "mercado")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appIcon = getAppIcon(context)
        
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID_RECORDATORIOS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        if (appIcon != null) {
            notificationBuilder.setLargeIcon(appIcon)
        }
        
        val notification = notificationBuilder.build()
        
        try {
            NotificationManagerCompat.from(context).notify(999, notification)
        } catch (e: SecurityException) {
            // Permiso no concedido
        }
    }
}
