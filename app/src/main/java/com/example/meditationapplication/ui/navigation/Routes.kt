package com.example.meditationapplication.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.meditationapplication.R

/** Маршрути NavHost (таблиця 2 курсової). */
object Routes {
    const val HOME = "home"
    const val LIBRARY = "library"
    const val TIMER = "timer"
    const val STATS = "stats"
    const val PLAYER_ARG_ID = "id"
    const val PLAYER = "player/{$PLAYER_ARG_ID}"
    const val SETTINGS = "settings"

    fun player(id: String) = "player/$id"
}

/** Вкладки нижньої панелі навігації — екрани верхнього рівня. */
enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(Routes.HOME, R.string.nav_home, Icons.Rounded.Home, Icons.Outlined.Home),
    LIBRARY(Routes.LIBRARY, R.string.nav_library, Icons.Rounded.SelfImprovement, Icons.Rounded.SelfImprovement),
    TIMER(Routes.TIMER, R.string.nav_timer, Icons.Rounded.Timer, Icons.Outlined.Timer),
    STATS(Routes.STATS, R.string.nav_stats, Icons.Rounded.BarChart, Icons.Rounded.BarChart),
}
