package datus.app.com.ui.notifications

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

import datus.app.com.ui.screens.DatusTopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import datus.app.com.R
import datus.app.com.ui.notifications.components.NotificationCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import java.util.Locale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.material.ExperimentalMaterialApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotificationsScreen(
    navController: NavHostController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Efecto para recargar las notificaciones automáticamente cuando la pantalla se vuelve visible.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadNotifications()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            DatusTopAppBar(title = "Notificaciones", navController = navController, canNavigateBack = true)
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text(
                text = "Toca una notificación para marcarla como leída",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = { viewModel.loadNotifications() })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Centra el contenido si la lista está vacía, o lo alinea arriba si tiene elementos.
                verticalArrangement = if (uiState.notifications.isEmpty()) Arrangement.Center else Arrangement.Top,
                horizontalAlignment = if (uiState.notifications.isEmpty()) Alignment.CenterHorizontally else Alignment.Start
            ) {
                if (uiState.notifications.isEmpty() && !uiState.isLoading) {
                    item {
                        EmptyNotificationsView()
                    }
                } else if (uiState.notifications.isNotEmpty()) {
                    val groupedNotifications = uiState.notifications.groupBy {
                        LocalDate.parse(it.createdAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    }
    
                    groupedNotifications.forEach { (date, notificationsForDate) ->
                        item {
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"))),
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(
                            items = notificationsForDate,
                            key = { it.id }
                        ) { notification ->
                            NotificationCard(
                                notification = notification,
                                isRead = notification.isRead,
                                onNotificationClicked = { viewModel.markAsRead(notification.id.toString()) },
                                onUrlClicked = { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }

                }
            }
            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}}

@Composable
private fun EmptyNotificationsView() {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isDark = isSystemInDarkTheme()
            val iconColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color.Gray
            Image(
                painter = painterResource(id = R.drawable.unarchive_24px),
                contentDescription = "Sin notificaciones",
                modifier = Modifier.size(64.dp),
                colorFilter = ColorFilter.tint(iconColor)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin notificaciones disponibles",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
