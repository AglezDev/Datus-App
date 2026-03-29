package datus.app.com.data

import kotlinx.serialization.Serializable

@Serializable
data class Promotion(
    val id: Int,
    val title: String,
    val description: String,
    val image_url: String,
    val button_text: String,
    val action_url: String? = null,
    val color: String? = null,
    val active: Boolean,
    val created_at: String
)