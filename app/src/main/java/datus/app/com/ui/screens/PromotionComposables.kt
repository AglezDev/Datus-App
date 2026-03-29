package datus.app.com.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.serialization.Serializable

// Modelo de datos para una promoción
@Serializable
data class Promotion(
    val id: Long,
    val title: String,
    val description: String,
    val image_url: String? = null,
    val button_text: String? = null,
    val action_url: String? = null,
    val color: String? = null,
    val active: Boolean,
    val created_at: String? = null
)

@Composable
fun PromoCardModernSolid(promo: Promotion) {
    val context = LocalContext.current
    val mainColor = try { Color(android.graphics.Color.parseColor(promo.color ?: "#007bff")) } catch (_: Exception) { Color(0xFF007bff) }
    
    // Use theme-aware text colors for better visibility in both light and dark modes
    val backgroundColor = MaterialTheme.colorScheme.surface
    val onBackgroundColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // Determine if the main color is light or dark for contrast
    val isMainColorLight = mainColor.luminance() > 0.5f
    
    // Use theme colors for better contrast in both light and dark modes
    val titleColor = onBackgroundColor
    val descriptionColor = onBackgroundColor.copy(alpha = 0.75f)
    val buttonTextColor = if (isMainColorLight) Color.Black else Color.White

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp) // More compact height
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Image on the left - increased width for better visibility
            if (!promo.image_url.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .width(120.dp) // Increased width for better visibility
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(promo.image_url)
                                .size(Size(240, 400))
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = promo.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // If no image, use the color as background on the left
                Box(
                    modifier = Modifier
                        .width(30.dp) // Increased width for better visibility
                        .fillMaxHeight()
                        .background(mainColor)
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                )
            }

            // Content on the right (title, description, button)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 14.dp), // Increased vertical padding
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = promo.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, // Increased font size
                        color = titleColor,
                        maxLines = 2, // Allow 2 lines for title
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = promo.description,
                        fontSize = 14.sp, // Increased font size
                        color = descriptionColor,
                        maxLines = 3, // Allow 3 lines for description
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (!promo.button_text.isNullOrBlank() && !promo.action_url.isNullOrBlank()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(promo.action_url))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // Increased padding
                        modifier = Modifier
                            .align(Alignment.Start)
                    ) {
                        Text(promo.button_text, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = buttonTextColor) // Use contrasting color
                    }
                }
            }
        }
    }
}