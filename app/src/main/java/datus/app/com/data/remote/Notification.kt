package datus.app.com.data.remote

import datus.app.com.data.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Notification(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val title: String,
    val details: String,
    val url: String? = null,
    val isRead: Boolean = false,
    @kotlinx.serialization.SerialName("created_at")
    val createdAt: String
)
