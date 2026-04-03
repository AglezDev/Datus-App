package datus.app.com

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

import datus.app.com.data.remote.Notification
import datus.app.com.ui.notifications.NotificationsViewModel
import datus.app.com.ui.notifications.NotificationsScreen
import datus.app.com.ui.notifications.components.CustomNotificationDisplay
import datus.app.com.ui.screens.CurrencyHistoryScreen
import datus.app.com.ui.screens.DatusTopAppBar
import datus.app.com.ui.screens.MenuScreen
import datus.app.com.ui.screens.MercadoScreen
import datus.app.com.ui.screens.NautaLoginScreen
import datus.app.com.ui.screens.NautaSettingsScreen
import datus.app.com.ui.screens.NotificationsSettingsScreen
import datus.app.com.ui.screens.PlansScreen
import datus.app.com.ui.screens.QueriesScreen
import datus.app.com.ui.screens.SettingsScreen
import datus.app.com.ui.screens.TarjetaScreen
import datus.app.com.ui.screens.UserSettingsScreen
import datus.app.com.ui.screens.UtilitiesScreen
import datus.app.com.ui.theme.DatusTheme
import datus.app.com.ui.theme.ThemeOption
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.ui.theme.ThemeViewModelFactory
import datus.app.com.utils.playClickSound
import androidx.navigation.NavType

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RequestPermissions()
            Box(modifier = Modifier.fillMaxSize()) {
                MainScreen(destinationFromIntent = intent.getStringExtra("destination"))
            }
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null,
    val selectedPainterResId: Int? = null,
    val unselectedPainterResId: Int? = null
) {
    object Queries : BottomNavItem(NavRoutes.QUERIES, "Consultas", 
        selectedIcon = Icons.Filled.Search, 
        unselectedIcon = Icons.Filled.Search)
    object Promotions : BottomNavItem(NavRoutes.MERCADO, "Mercado", 
        selectedIcon = Icons.Filled.CurrencyExchange, 
        unselectedIcon = Icons.Filled.CurrencyExchange)
    object Utilities : BottomNavItem(NavRoutes.UTILITIES, "Útiles", 
        selectedIcon = Icons.Filled.Widgets, 
        unselectedIcon = Icons.Filled.Widgets)
    object Plans : BottomNavItem(NavRoutes.PLANS, "Planes", 
        selectedIcon = Icons.Filled.DataUsage, 
        unselectedIcon = Icons.Filled.DataUsage)
    object Nauta : BottomNavItem(NavRoutes.NAUTA_LOGIN, "Nauta", 
        selectedIcon = Icons.Filled.Wifi, 
        unselectedIcon = Icons.Filled.Wifi)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    destinationFromIntent: String? = null,
    themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(LocalContext.current)),
    settingsViewModel: datus.app.com.ui.screens.SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notificationsViewModel: datus.app.com.ui.notifications.NotificationsViewModel = hiltViewModel()
    val uiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

    val theme by themeViewModel.theme.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    
    // Handle navigation from widget click
    LaunchedEffect(destinationFromIntent, navController) {
        if (destinationFromIntent == "mercado") {
            navController.navigate(NavRoutes.MERCADO) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    val bottomNavItems = listOf(BottomNavItem.Queries, BottomNavItem.Promotions, BottomNavItem.Plans, BottomNavItem.Nauta, BottomNavItem.Utilities)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsStateWithLifecycle()

    if (currentRoute != NavRoutes.NOTIFICATIONS && notificationsEnabled) {
        uiState.initialNotification?.let { notification ->
            AlertDialog(
                onDismissRequest = { notificationsViewModel.onInitialNotificationDismissed() },
                title = { Text(notification.title) },
                text = { Text(notification.details) },
                confirmButton = {
                    TextButton(onClick = { 
                        notificationsViewModel.markAsRead(notification.id.toString())
                        notificationsViewModel.onInitialNotificationDismissed()
                    }) {
                        Text("Marcar como leída")
                    }
                },
                dismissButton = {
                    if (!notification.url.isNullOrEmpty()) {
                        TextButton(onClick = { 
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notification.url))
                            context.startActivity(intent)
                            notificationsViewModel.onInitialNotificationDismissed()
                        }) {
                            Text("Detalles")
                        }
                    }
                }
            )
        }
    }

    val unreadCount by notificationsViewModel.unreadCount.collectAsStateWithLifecycle()

    LaunchedEffect(navController) {
        snapshotFlow { navController.currentDestination?.route }
            .distinctUntilChanged()
            .collect { route ->
                if (route != NavRoutes.NOTIFICATIONS) {
                    notificationsViewModel.loadNotifications()
                }
            }
    }

    DatusTheme(
        darkTheme = when (theme) {
            ThemeOption.LIGHT -> false
            ThemeOption.DARK -> true
            ThemeOption.AUTO -> isSystemInDarkTheme()
        }
    ) {
        Scaffold(
            bottomBar = @Composable {
                if (showBottomBar) {
                    AppBottomNavigation(navController = navController, items = bottomNavItems, unreadCount = unreadCount)
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavigationGraph(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding),
                    themeViewModel = themeViewModel,
                    notificationsViewModel = notificationsViewModel
                )

                
            }
        }
    }
}

@Composable
fun AppBottomNavigation(navController: NavHostController, items: List<BottomNavItem>, unreadCount: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val view = LocalView.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val selectedItemIndex by remember {
        derivedStateOf {
            items.indexOfFirst { screen ->
                currentDestination?.hierarchy?.any { it.route == screen.route } == true
            }.coerceAtLeast(0)
        }
    }

    NavigationBar(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), clip = false)
            .border(
                width = 1.dp,
                color = colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        containerColor = colorScheme.surface,
        contentColor = colorScheme.onSurface
    ) {
        items.forEachIndexed { index, screen ->
            val selected = selectedItemIndex == index
            val isPlansIcon = screen.route == NavRoutes.PLANS

            // Icono ligeramente más grande para Plans
            val iconModifier = if (isPlansIcon) {
                Modifier.size(28.dp)
            } else {
                Modifier
            }

            // Colores específicos para modo claro y oscuro
            val iconTint = if (selected) {
                colorScheme.secondary // Icono activo = color secondary
            } else {
                colorScheme.onSurfaceVariant // Icono inactivo = color más suave
            }

            val labelColor = if (selected) {
                colorScheme.secondary
            } else {
                colorScheme.onSurfaceVariant
            }

            NavigationBarItem(
                icon = {
                    if (screen.selectedIcon != null && screen.unselectedIcon != null) {
                        Icon(
                            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title,
                            modifier = iconModifier,
                            tint = iconTint
                        )
                    } else if (screen.selectedPainterResId != null && screen.unselectedPainterResId != null) {
                        Icon(
                            painter = painterResource(id = if (selected) screen.selectedPainterResId else screen.unselectedPainterResId),
                            contentDescription = screen.title,
                            modifier = iconModifier,
                            tint = iconTint
                        )
                    }
                },
                label = {
                    Text(screen.title, softWrap = false, color = labelColor)
                },
                selected = selected,
                alwaysShowLabel = false,
                onClick = {
                    playClickSound(view)
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    selectedIconColor = colorScheme.secondary,
                    selectedTextColor = colorScheme.secondary,
                    unselectedIconColor = colorScheme.onSurfaceVariant,
                    unselectedTextColor = colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
@Composable
fun AppNavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel,
    notificationsViewModel: datus.app.com.ui.notifications.NotificationsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.QUERIES,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(NavRoutes.PLANS) { PlansScreen(navController, themeViewModel) }
        composable(NavRoutes.QUERIES) { QueriesScreen(navController, themeViewModel) }
        composable(NavRoutes.UTILITIES) { UtilitiesScreen(navController, themeViewModel) }
        composable(NavRoutes.MENU) { MenuScreen(navController, themeViewModel) }
        composable(NavRoutes.SETTINGS) { SettingsScreen(navController, themeViewModel) }
        composable(NavRoutes.NOTIFICATION_SETTINGS) { NotificationsSettingsScreen(navController) }
        composable(
            route = NavRoutes.USER_SETTINGS,
            arguments = listOf(navArgument("focusPhoneNumber") { defaultValue = false })
        ) { backStackEntry ->
            val focusPhoneNumber = backStackEntry.arguments?.getBoolean("focusPhoneNumber") ?: false
            UserSettingsScreen(navController, themeViewModel, focusPhoneNumber)
        }
        composable(NavRoutes.MERCADO) { 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                MercadoScreen(navController = navController)
            }
        }
        composable(NavRoutes.NOTIFICATIONS) { NotificationsScreen(navController = navController) }
        composable(NavRoutes.CARD) { TarjetaScreen(navController = navController, themeViewModel = themeViewModel) }
        composable(
            route = NavRoutes.CURRENCY_HISTORY,
            arguments = listOf(navArgument("currency") { type = NavType.StringType })
        ) { backStackEntry ->
            val currency = backStackEntry.arguments?.getString("currency") ?: ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CurrencyHistoryScreen(currency = currency, navController = navController)
            }
        }
        composable(NavRoutes.NAUTA_LOGIN) { NautaLoginScreen(navController = navController, themeViewModel = themeViewModel) }
        composable(NavRoutes.NAUTA_SETTINGS) { NautaSettingsScreen(navController = navController) }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DatusTheme {
        val notificationsViewModel: datus.app.com.ui.notifications.NotificationsViewModel = hiltViewModel()
        val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(LocalContext.current))
        MainScreen(themeViewModel = themeViewModel)
    }
}

@Composable
fun RequestPermissions() {
    val context = LocalContext.current

    val permissionsToRequest = mutableListOf<String>()
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
        permissionsToRequest.add(Manifest.permission.CALL_PHONE)
    }
    

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            permissions.entries.forEach { (permission, isGranted) ->
                Log.d("Permissions", "Permission: $permission, Granted: $isGranted")
            }
        }
    )

    LaunchedEffect(Unit) {
        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest.toTypedArray())
        }
    }
}