package datus.app.com

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TransferWithinAStation
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewArray
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewComfy
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.Vignette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VoiceChat
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbAuto
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiCalling
import androidx.compose.material.icons.filled.WifiLock
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material.icons.filled.Wysiwyg
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.AlertDialog // Added import
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Added import
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import datus.app.com.ui.notifications.components.CustomNotificationDisplay
import datus.app.com.data.remote.Notification
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent // Added import
import android.net.Uri // Added import
import androidx.navigation.navArgument

import datus.app.com.ui.notifications.NotificationsScreen
import datus.app.com.ui.screens.PlansScreen
import datus.app.com.ui.screens.QueriesScreen
import datus.app.com.ui.screens.UtilitiesScreen
import datus.app.com.ui.screens.MenuScreen
import datus.app.com.ui.screens.SettingsScreen
import datus.app.com.ui.screens.MercadoScreen
import datus.app.com.ui.theme.ThemeOption
import datus.app.com.ui.theme.DatusTheme
import datus.app.com.ui.theme.ThemeViewModel
import datus.app.com.ui.theme.ThemeViewModelFactory
import datus.app.com.utils.playClickSound
import datus.app.com.ui.screens.DatusTopAppBar
import datus.app.com.ui.screens.UserSettingsScreen
import datus.app.com.ui.screens.TarjetaScreen
import datus.app.com.ui.screens.CurrencyHistoryScreen
import datus.app.com.ui.screens.NotificationsSettingsScreen
import androidx.navigation.NavType
import kotlinx.coroutines.delay

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
        selectedIcon = Icons.Filled.CollectionsBookmark, 
        unselectedIcon = Icons.Filled.CollectionsBookmark)
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
    val bottomNavItems = listOf(BottomNavItem.Queries, BottomNavItem.Promotions, BottomNavItem.Plans, BottomNavItem.Utilities)
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
        while (true) {
            delay(60_000L) // Recargar cada 1 minuto
            if (navController.currentDestination?.route != NavRoutes.NOTIFICATIONS) {
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
    NavigationBar(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), clip = false)
            .border(
                width = 1.dp,
                color = colorScheme.onSurface.copy(alpha = 0.10f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
        containerColor = colorScheme.surface,
        contentColor = colorScheme.onSurface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = {
                    if (screen.selectedIcon != null && screen.unselectedIcon != null) {
                        Icon(
                            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title
                        )
                    } else if (screen.selectedPainterResId != null && screen.unselectedPainterResId != null) {
                        Icon(
                            painter = painterResource(id = if (selected) screen.selectedPainterResId else screen.unselectedPainterResId),
                            contentDescription = screen.title
                        )
                    }
                },
                label = { Text(screen.title, softWrap = false) },
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
                    indicatorColor = colorScheme.surface,
                    selectedIconColor = colorScheme.primary,
                    selectedTextColor = colorScheme.primary,
                    unselectedIconColor = colorScheme.onSurface,
                    unselectedTextColor = colorScheme.onSurface
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