package com.example.meditationapplication.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Світла тема (таблиця 3 курсової)
val Primary = Color(0xFF5E72B8)
val PrimaryContainer = Color(0xFFE3E7F7)
val Background = Color(0xFFF6F7FB)
val Surface = Color(0xFFFFFFFF)
val Outline = Color(0xFFD5D8E2)
val OnSurface = Color(0xFF1F2333)
val OnSurfaceVariant = Color(0xFF6B7080)
val Favorite = Color(0xFFD9596B)

// Темна тема: ті самі токени з темними значеннями
val PrimaryDark = Color(0xFF9AA8E8)
val PrimaryContainerDark = Color(0xFF2F3758)
val BackgroundDark = Color(0xFF121420)
val SurfaceDark = Color(0xFF1E2130)
val OutlineDark = Color(0xFF353A4E)
val OnSurfaceDark = Color(0xFFE6E8F0)
val OnSurfaceVariantDark = Color(0xFFA2A7B8)
val FavoriteDark = Color(0xFFE77A8A)

/** Колір вогника серії днів. */
val StreakFlame = Color(0xFFF0954A)

/** Додатковий токен favorite (заповнена іконка «Обране»). */
val ColorScheme.favorite: Color
    get() = if (background.luminance() > 0.5f) Favorite else FavoriteDark
