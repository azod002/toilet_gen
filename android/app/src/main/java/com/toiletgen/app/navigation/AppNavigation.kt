package com.toiletgen.app.navigation

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.repository.AuthRepository
import com.toiletgen.feature.achievements.ui.AchievementsScreen
import com.toiletgen.feature.auth.ui.AuthScreen
import com.toiletgen.feature.chat.ui.ChatHubScreen
import com.toiletgen.feature.chat.ui.GlobalChatScreen
import com.toiletgen.feature.chat.ui.NewChatScreen
import com.toiletgen.feature.chat.ui.PrivateChatScreen
import com.toiletgen.feature.chat.viewmodel.PrivateChatViewModel
import com.toiletgen.feature.entertainment.ui.BooksScreen
import com.toiletgen.feature.entertainment.ui.EntertainmentScreen
import com.toiletgen.feature.entertainment.ui.JokesScreen
import com.toiletgen.feature.entertainment.ui.NewsScreen
import com.toiletgen.feature.entertainment.ui.ForumScreen
import com.toiletgen.feature.entertainment.ui.RadioScreen
import com.toiletgen.feature.entertainment.ui.ThreadDetailScreen
import com.toiletgen.feature.entertainment.viewmodel.ThreadDetailViewModel
import com.toiletgen.feature.map.ui.AddToiletScreen
import com.toiletgen.feature.map.ui.MapScreen
import com.toiletgen.feature.profile.ui.ProfileScreen
import com.toiletgen.feature.profile.ui.VisitHistoryScreen
import com.toiletgen.feature.sos.ui.SosScreen
import com.toiletgen.feature.toilet_details.ui.AddReviewScreen
import com.toiletgen.feature.toilet_details.ui.ToiletDetailsScreen
import com.toiletgen.feature.stamps.ui.StampsScreen
import com.toiletgen.feature.yearly_report.ui.YearlyReportScreen
import com.toiletgen.app.ui.ConsentScreen
import kotlinx.coroutines.flow.firstOrNull
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Consent : Screen("consent", "Согласие")
    data object Auth : Screen("auth", "Авторизация")
    data object Map : Screen("map", "Карта", Icons.Default.Map)
    data object Profile : Screen("profile", "Профиль", Icons.Default.Person)
    data object Entertainment : Screen("entertainment", "Развлечения", Icons.Default.SportsEsports)
    data object Chat : Screen("chat", "Чат", Icons.Default.Chat)
    data object Report : Screen("report", "Отчёт", Icons.Default.Assessment)
    data object ToiletDetails : Screen("toilet/{toiletId}", "Детали")
    data object SOS : Screen("sos/{lat}/{lon}", "SOS")
    data object Achievements : Screen("achievements", "Достижения")
    data object AddToilet : Screen("add-toilet/{lat}/{lon}", "Добавить точку")
    data object AddReview : Screen("add-review/{toiletId}", "Написать отзыв")
    data object VisitHistory : Screen("visit-history", "История посещений")
    data object Stamps : Screen("stamps", "Марки")
    data object News : Screen("entertainment/news", "Новости")
    data object Radio : Screen("entertainment/radio", "Радио")
    data object Jokes : Screen("entertainment/jokes", "Анекдоты")
    data object Books : Screen("entertainment/books", "Книги")
    data object GlobalChat : Screen("chat/global", "Общий чат")
    data object PrivateChat : Screen("chat/private/{userId}/{username}", "Личный чат")
    data object NewChat : Screen("chat/new", "Новый чат")
    data object Forum : Screen("entertainment/forum", "Форум")
    data object ThreadDetail : Screen("entertainment/forum/{threadId}", "Тред")
}

val bottomNavItems = listOf(Screen.Map, Screen.Entertainment, Screen.Profile, Screen.Report)

// Анимации для вложенных экранов (slide in/out)
private fun slideInFromRight() = slideInHorizontally(tween(350)) { it } + fadeIn(tween(300))
private fun slideOutToRight() = slideOutHorizontally(tween(350)) { it } + fadeOut(tween(300))
private fun slideInFromLeft() = slideInHorizontally(tween(350)) { -it } + fadeIn(tween(300))
private fun slideOutToLeft() = slideOutHorizontally(tween(350)) { -it } + fadeOut(tween(300))

// Анимации для табов (fade)
private fun tabFadeIn() = fadeIn(tween(250))
private fun tabFadeOut() = fadeOut(tween(200))

// Анимации для оверлеев (SOS) — slide up
private fun slideUp() = slideInVertically(tween(400)) { it } + fadeIn(tween(350))
private fun slideDown() = slideOutVertically(tween(400)) { it } + fadeOut(tween(350))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    tokenProvider: com.toiletgen.core.network.TokenProvider = org.koin.compose.koinInject(),
    authRepository: AuthRepository = org.koin.compose.koinInject(),
) {
    val context = LocalContext.current
    val hasToken = remember { tokenProvider.getAccessToken() != null }
    val hasConsent = remember {
        context.getSharedPreferences("consent", Context.MODE_PRIVATE)
            .getBoolean("privacy_accepted", false)
    }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Get current user ID for private chat
    var currentUserId by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val resource = authRepository.getCurrentUser().firstOrNull()
        if (resource is Resource.Success) {
            currentUserId = resource.data.id
        }
    }

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = when {
                !hasConsent -> Screen.Consent.route
                hasToken -> Screen.Map.route
                else -> Screen.Auth.route
            },
            modifier = Modifier.padding(padding),
            enterTransition = { tabFadeIn() },
            exitTransition = { tabFadeOut() },
            popEnterTransition = { tabFadeIn() },
            popExitTransition = { tabFadeOut() },
        ) {
            // Consent screen
            composable(
                Screen.Consent.route,
                enterTransition = { fadeIn(tween(500)) },
                exitTransition = { fadeOut(tween(300)) },
            ) {
                ConsentScreen(onAccepted = {
                    val dest = if (hasToken) Screen.Map.route else Screen.Auth.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Consent.route) { inclusive = true }
                    }
                })
            }

            // Auth — fade in/out
            composable(
                Screen.Auth.route,
                enterTransition = { fadeIn(tween(500)) },
                exitTransition = { fadeOut(tween(300)) },
            ) {
                AuthScreen(onAuthenticated = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                })
            }

            // Табы — мягкий fade
            composable(Screen.Map.route) {
                // Refresh toilets when returning from add-toilet screen
                val shouldRefresh = navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.getStateFlow("toiletAdded", false)
                    ?.collectAsState()
                val mapViewModel: com.toiletgen.feature.map.viewmodel.MapViewModel = org.koin.androidx.compose.koinViewModel()

                LaunchedEffect(shouldRefresh?.value) {
                    if (shouldRefresh?.value == true) {
                        mapViewModel.refreshToilets()
                        navController.currentBackStackEntry?.savedStateHandle?.set("toiletAdded", false)
                    }
                }

                MapScreen(
                    onToiletClick = { id -> navController.navigate("toilet/$id") },
                    onSosClick = { navController.navigate("sos/55.7558/37.6173") },
                    onAddToiletClick = { lat, lon ->
                        navController.navigate("add-toilet/$lat/$lon")
                    },
                    viewModel = mapViewModel,
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onAchievementsClick = { navController.navigate(Screen.Achievements.route) },
                    onYearlyReportClick = { navController.navigate(Screen.Report.route) },
                    onVisitHistoryClick = { navController.navigate(Screen.VisitHistory.route) },
                    onStampsClick = { navController.navigate(Screen.Stamps.route) },
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable(Screen.Entertainment.route) {
                EntertainmentScreen(
                    onNavigate = { route -> navController.navigate(route) },
                )
            }

            composable(Screen.Report.route) {
                YearlyReportScreen()
            }

            // Chat hub (sub-screen of entertainment)
            composable(
                Screen.Chat.route,
                enterTransition = { slideInFromRight() },
                popExitTransition = { slideOutToRight() },
            ) {
                ChatHubScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToGlobalChat = { navController.navigate(Screen.GlobalChat.route) },
                    onNavigateToPrivateChat = { userId, username ->
                        navController.navigate("chat/private/$userId/$username")
                    },
                    onStartNewChat = { navController.navigate(Screen.NewChat.route) },
                )
            }

            // Global chat
            composable(
                Screen.GlobalChat.route,
                enterTransition = { slideInFromRight() },
                popExitTransition = { slideOutToRight() },
            ) {
                GlobalChatScreen(
                    onBack = { navController.popBackStack() },
                    onUserClick = { userId, username ->
                        navController.navigate("chat/private/$userId/$username")
                    },
                )
            }

            // Private chat
            composable(
                Screen.PrivateChat.route,
                enterTransition = { slideInFromRight() },
                popExitTransition = { slideOutToRight() },
            ) { backStack ->
                val partnerId = backStack.arguments?.getString("userId") ?: return@composable
                val partnerUsername = backStack.arguments?.getString("username") ?: return@composable
                val viewModel: PrivateChatViewModel = koinViewModel { parametersOf(partnerId, partnerUsername) }
                PrivateChatScreen(
                    partnerUsername = partnerUsername,
                    currentUserId = currentUserId,
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel,
                )
            }

            // New chat
            composable(
                Screen.NewChat.route,
                enterTransition = { slideInFromRight() },
                popExitTransition = { slideOutToRight() },
            ) {
                NewChatScreen(
                    onBack = { navController.popBackStack() },
                    onStartChat = { userId, username ->
                        navController.navigate("chat/private/$userId/$username") {
                            popUpTo(Screen.Chat.route)
                        }
                    },
                )
            }

            // Entertainment sub-screens
            composable(
                Screen.News.route,
                enterTransition = { slideInFromRight() },
                popExitTransition = { slideOutToRight() },
            ) {
                NewsScreen(onBack = { navController.popBackStack() })
            }

            composable(
                Screen.Radio.route,
                enterTransition = { slideInFromRight() },
                popExitTransition = { slideOutToRight() },
            ) {
                RadioScreen(onBack = { navController.popBackStack() })
            }

            composable(
                Screen.Jokes.route,
                enterTransition = { slideInFromRight() },
                popExitTransition = { slideOutToRight() },
            ) {
                JokesScreen(onBack = { navController.popBackStack() })
            }

            composable(
                Screen.Books.route,
                enterTransition = { slideInFromRight() },
                popExitTransition = { slideOutToRight() },
            ) {
                BooksScreen(onBack = { navController.popBackStack() })
            }

            // Forum
            composable(
                Screen.Forum.route,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                ForumScreen(
                    onBack = { navController.popBackStack() },
                    onThreadClick = { threadId ->
                        navController.navigate("entertainment/forum/$threadId")
                    },
                )
            }

            // Thread detail
            composable(
                Screen.ThreadDetail.route,
                enterTransition = { slideInFromRight() },
                popExitTransition = { slideOutToRight() },
            ) { backStack ->
                val threadId = backStack.arguments?.getString("threadId") ?: return@composable
                val viewModel: ThreadDetailViewModel = koinViewModel { parametersOf(threadId) }
                ThreadDetailScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel,
                )
            }

            // Вложенные экраны — slide in/out
            composable(
                "toilet/{toiletId}",
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) { backStack ->
                val toiletId = backStack.arguments?.getString("toiletId") ?: return@composable
                ToiletDetailsScreen(
                    toiletId = toiletId,
                    onBack = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("toiletAdded", true)
                        navController.popBackStack()
                    },
                    onAddReview = { navController.navigate("add-review/$toiletId") },
                )
            }

            composable(
                "add-toilet/{lat}/{lon}",
                enterTransition = { slideUp() },
                exitTransition = { slideDown() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideDown() },
            ) { backStack ->
                val lat = backStack.arguments?.getString("lat")?.toDoubleOrNull() ?: 55.7558
                val lon = backStack.arguments?.getString("lon")?.toDoubleOrNull() ?: 37.6173
                AddToiletScreen(
                    latitude = lat,
                    longitude = lon,
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.previousBackStackEntry?.savedStateHandle?.set("toiletAdded", true)
                        navController.popBackStack()
                    },
                )
            }

            composable(
                "add-review/{toiletId}",
                enterTransition = { slideUp() },
                exitTransition = { slideDown() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideDown() },
            ) { backStack ->
                val toiletId = backStack.arguments?.getString("toiletId") ?: return@composable
                AddReviewScreen(
                    toiletId = toiletId,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() },
                )
            }

            // SOS — slide up from bottom
            composable(
                "sos/{lat}/{lon}",
                enterTransition = { slideUp() },
                exitTransition = { slideDown() },
                popExitTransition = { slideDown() },
            ) { backStack ->
                val lat = backStack.arguments?.getString("lat")?.toDoubleOrNull() ?: 55.7558
                val lon = backStack.arguments?.getString("lon")?.toDoubleOrNull() ?: 37.6173
                SosScreen(userLat = lat, userLon = lon, onClose = { navController.popBackStack() })
            }

            composable(
                Screen.Achievements.route,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                AchievementsScreen(onBack = { navController.popBackStack() })
            }

            composable(
                Screen.VisitHistory.route,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                VisitHistoryScreen(onBack = { navController.popBackStack() })
            }

            composable(
                Screen.Stamps.route,
                enterTransition = { slideInFromRight() },
                exitTransition = { slideOutToLeft() },
                popEnterTransition = { slideInFromLeft() },
                popExitTransition = { slideOutToRight() },
            ) {
                StampsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
