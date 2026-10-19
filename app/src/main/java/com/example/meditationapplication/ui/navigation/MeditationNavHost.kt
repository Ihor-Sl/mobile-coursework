package com.example.meditationapplication.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.meditationapplication.ui.home.HomeScreen
import com.example.meditationapplication.ui.library.LibraryFilter
import com.example.meditationapplication.ui.library.LibraryScreen
import com.example.meditationapplication.ui.library.LibraryViewModel
import com.example.meditationapplication.ui.player.PlayerScreen
import com.example.meditationapplication.ui.settings.SettingsScreen
import com.example.meditationapplication.ui.stats.StatsScreen
import com.example.meditationapplication.ui.timer.TimerScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * Каркас додатка: NavHost і нижня панель навігації.
 * Панель показується лише на екранах верхнього рівня; Плеєр і Налаштування відкриваються поверх.
 */
@Composable
fun MeditationAppUi() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTab = TopLevelDestination.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }
    // Повторне натискання на активну вкладку прокручує її вміст угору
    val reselectedTab = remember { MutableSharedFlow<String>(extraBufferCapacity = 1) }
    fun scrollToTopOf(route: String): Flow<Unit> = reselectedTab.filter { it == route }.map { }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentTab != null) {
                AppBottomBar(
                    current = currentTab,
                    onSelect = { tab ->
                        if (tab == currentTab) reselectedTab.tryEmit(tab.route)
                        else navController.navigateToTab(tab.route)
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenMeditation = { id -> navController.navigate(Routes.player(id)) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenFavorites = {
                        navController.navigateToTab(Routes.LIBRARY)
                        navController.getBackStackEntry(Routes.LIBRARY)
                            .savedStateHandle[LibraryViewModel.FILTER_KEY] = LibraryFilter.FAVORITES.name
                    },
                    scrollToTop = remember { scrollToTopOf(Routes.HOME) },
                )
            }
            composable(Routes.LIBRARY) {
                LibraryScreen(
                    onOpenMeditation = { id -> navController.navigate(Routes.player(id)) },
                    scrollToTop = remember { scrollToTopOf(Routes.LIBRARY) },
                )
            }
            composable(Routes.TIMER) {
                TimerScreen(onOpenStats = { navController.navigateToTab(Routes.STATS) })
            }
            composable(Routes.STATS) {
                StatsScreen(
                    onGoHome = { navController.navigateToTab(Routes.HOME) },
                    scrollToTop = remember { scrollToTopOf(Routes.STATS) },
                )
            }
            composable(
                route = Routes.PLAYER,
                arguments = listOf(navArgument(Routes.PLAYER_ARG_ID) { type = NavType.StringType }),
            ) { entry ->
                PlayerScreen(
                    meditationId = entry.arguments?.getString(Routes.PLAYER_ARG_ID).orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenStats = {
                        navController.popBackStack()
                        navController.navigateToTab(Routes.STATS)
                    },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/**
 * Перехід на вкладку: стек над Головною зберігається і очищується, тому
 * «Назад» на будь-якій вкладці повертає на Головну, а на Головній — закриває додаток.
 */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun AppBottomBar(
    current: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            TopLevelDestination.entries.forEach { tab ->
                val selected = tab == current
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(tab) },
                    icon = {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tab.labelRes),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
