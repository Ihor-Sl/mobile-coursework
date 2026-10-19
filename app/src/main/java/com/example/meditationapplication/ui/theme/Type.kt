package com.example.meditationapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

/**
 * Шрифт Roboto (системний), стандартна шкала Material 3 (таблиця 4).
 * Заголовки трохи жирніші за замовчуванням.
 */
private val Base = Typography()

val Typography = Typography(
    displayMedium = Base.displayMedium,                                   // 45 sp — цифри таймера
    headlineSmall = Base.headlineSmall.copy(fontWeight = FontWeight.SemiBold), // 24 sp — заголовок екрана
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold),  // 22 sp — назва в плеєрі
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold), // 16 sp — картки, секції
    bodyLarge = Base.bodyLarge,                                           // 16 sp — пункти налаштувань
    bodyMedium = Base.bodyMedium,                                         // 14 sp — описи
    labelMedium = Base.labelMedium,                                       // 12 sp — вкладки, тривалість
)
