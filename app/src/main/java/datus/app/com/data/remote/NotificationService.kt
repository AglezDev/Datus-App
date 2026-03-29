package datus.app.com.data.remote

import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

import io.github.jan.supabase.postgrest.query.Order

class NotificationService @Inject constructor(private val postgrest: Postgrest) {

    suspend fun getNotifications(): List<Notification> {
        return postgrest.from("notificaciones").select { order("created_at", Order.ASCENDING) }.decodeList<Notification>()
    }
}