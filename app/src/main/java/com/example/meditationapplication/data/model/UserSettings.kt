package com.example.meditationapplication.data.model

import java.time.LocalTime

enum class AppTheme { SYSTEM, LIGHT, DARK }

/** Налаштування користувача, які зберігаються у DataStore. */
data class UserSettings(
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(8, 0),
    val theme: AppTheme = AppTheme.SYSTEM,
)
