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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Widgets
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.LayoutDirection
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer

import datus.app.com.data.remote.Notification
import datus.app.com.ui.notifications.NotificationsViewModel
import datus.app.com.ui.notifications.NotificationsScreen
import datus.app.com.ui.notifications.components.CustomNotificationDisplay
import datus.app.com.ui.screens.CurrencyHistoryScreen
import datus.app.com.ui.screens.DatusTopAppBar
import datus.app.com.ui.screens.MenuScreen
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

import androidx.activity.enableEdgeToEdge

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RequestPermissions()
            Box(modifier = Modifier.fillMaxSize()) {
                MainScreen()
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
        unselectedIcon = Icons.Outlined.Search)
    object Utilities : BottomNavItem(NavRoutes.UTILITIES, "Útiles",
        selectedIcon = Icons.Filled.Widgets,
        unselectedIcon = Icons.Outlined.Widgets)
    object Plans : BottomNavItem(NavRoutes.PLANS, "Planes",
        selectedIcon = Icons.Filled.DataUsage,
        unselectedIcon = Icons.Outlined.DataUsage)
    object Nauta : BottomNavItem(NavRoutes.NAUTA_LOGIN, "Nauta",
        selectedIcon = Icons.Filled.Wifi,
        unselectedIcon = Icons.Outlined.Wifi)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(LocalContext.current)),
    settingsViewModel: datus.app.com.ui.screens.SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notificationsViewModel: datus.app.com.ui.notifications.NotificationsViewModel = hiltViewModel()
    val uiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

    val theme by themeViewModel.theme.collectAsStateWithLifecycle()
    val useDynamicColor by themeViewModel.useDynamicColor.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val bottomNavItems = listOf(BottomNavItem.Queries, BottomNavItem.Plans, BottomNavItem.Nauta, BottomNavItem.Utilities)
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

    // Debounced notification reload: only reload every 30s max
    var lastNotificationReload by remember { mutableStateOf(0L) }
    LaunchedEffect(navController) {
        snapshotFlow { navController.currentDestination?.route }
            .distinctUntilChanged()
            .collect { route ->
                if (route != NavRoutes.NOTIFICATIONS) {
                    val now = System.currentTimeMillis()
                    if (now - lastNotificationReload > 30_000) {
                        lastNotificationReload = now
                        notificationsViewModel.loadNotifications()
                    }
                }
            }
    }

    DatusTheme(
        darkTheme = when (theme) {
            ThemeOption.LIGHT -> false
            ThemeOption.DARK -> true
            ThemeOption.AMOLED -> true
            ThemeOption.AUTO -> isSystemInDarkTheme()
        },
        useDynamicColor = useDynamicColor,
        useAmoledDark = theme == ThemeOption.AMOLED
    ) {
        Scaffold(
            bottomBar = @Composable {
                if (showBottomBar) {
                    AppBottomNavigation(navController = navController, items = bottomNavItems, unreadCount = unreadCount)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            if (showBottomBar) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    color = MaterialTheme.colorScheme.background, // Usamos background directamente
                    tonalElevation = 0.dp // Forzamos 0dp para evitar tintes de elevación
                ) {
                    AppNavigationGraph(
                        navController = navController,
                        modifier = Modifier.padding(
                            top = innerPadding.calculateTopPadding(),
                            start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                            end = innerPadding.calculateRightPadding(LayoutDirection.Ltr)
                        ),
                        themeViewModel = themeViewModel,
                        notificationsViewModel = notificationsViewModel
                    )
                }
            } else {
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

    val selectedItemIndex = items.indexOfFirst { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }.coerceAtLeast(0)

    // Contenedor de la barra flotante
    val navigationInsets = WindowInsets.navigationBars.asPaddingValues()
    val bottomPadding = navigationInsets.calculateBottomPadding()
    
    Surface(
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .padding(bottom = bottomPadding + 16.dp) // Elevación extra sobre la barra del sistema
            .fillMaxWidth()
            .height(68.dp), 
        shape = androidx.compose.foundation.shape.CircleShape,
        color = colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            items.forEachIndexed { index, screen ->
                val selected = selectedItemIndex == index
                
                NavigationBarItem(
                    icon = {
                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.1f else 1f,
                            animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium),
                            label = "iconScale"
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(if (selected) 40.dp else 34.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                                    .background(
                                        color = if (selected) colorScheme.primaryContainer.copy(alpha = 0.6f) else Color.Transparent,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            ) {
                                val iconModifier = Modifier.size(if (selected) 24.dp else 20.dp)
                                if (screen.selectedIcon != null && screen.unselectedIcon != null) {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title,
                                        modifier = iconModifier,
                                        tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                } else if (screen.selectedPainterResId != null && screen.unselectedPainterResId != null) {
                                    Icon(
                                        painter = painterResource(id = if (selected) screen.selectedPainterResId else screen.unselectedPainterResId),     
                                        contentDescription = screen.title,
                                        modifier = iconModifier,
                                        tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            
                            if (!selected) {
                                Text(
                                    screen.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    label = { /* Usamos el Column del icon para control total del espaciado */ },
                    selected = selected,
                    alwaysShowLabel = false, 
                    onClick = {
                        if (!selected) {
                            playClickSound(view)
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent, // Eliminamos el pill residual
                        selectedIconColor = colorScheme.primary,
                        unselectedIconColor = colorScheme.onSurfaceVariant
                    )
                )
            }
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
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / 10 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(300))
        },
        exitTransition = {
            fadeOut(tween(250))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 10 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it / 10 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(tween(250))
        },
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
