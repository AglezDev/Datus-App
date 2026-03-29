package datus.app.com.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.compose.ui.unit.dp
import datus.app.com.data.remote.Notification
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight

@Composable
fun NotificationCard(
    notification: Notification,
    isRead: Boolean,
    onNotificationClicked: (String) -> Unit,
    onUrlClicked: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onNotificationClicked(notification.id.toString()) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.Bold,
                    color = if (isRead) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notification.details,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isRead) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            )
            Log.d("NotificationCard", "Notification ID: ${notification.id}, URL: ${notification.url}")
            if (!notification.url.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onUrlClicked(notification.url) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Detalles")
                }
            }
        }
    }
}