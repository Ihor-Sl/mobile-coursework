package com.example.meditationapplication.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.meditationapplication.data.model.AppTheme
import com.example.meditationapplication.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

/** Налаштування користувача (нагадування, тема). */
interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setReminder(enabled: Boolean, time: LocalTime)

    suspend fun setTheme(theme: AppTheme)
}

/** Файл settings.preferences_pb. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    /** Час нагадування у хвилинах від початку доби (0–1439). */
    val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
    val THEME = stringPreferencesKey("theme")

    const val DEFAULT_REMINDER_MINUTES = 480 // 08:00
}

class DataStoreSettingsRepository(context: Context) : SettingsRepository {

    private val dataStore = context.applicationContext.dataStore

    override val settings: Flow<UserSettings> = dataStore.data.map { p ->
        UserSettings(
            reminderEnabled = p[SettingsKeys.REMINDER_ENABLED] ?: false,
            reminderTime = LocalTime.MIN.plusMinutes(
                (p[SettingsKeys.REMINDER_MINUTES] ?: SettingsKeys.DEFAULT_REMINDER_MINUTES).toLong()
            ),
            theme = p[SettingsKeys.THEME]
                ?.let { name -> AppTheme.entries.firstOrNull { it.name == name } }
                ?: AppTheme.SYSTEM,
        )
    }

    override suspend fun setReminder(enabled: Boolean, time: LocalTime) {
        dataStore.edit { p ->
            p[SettingsKeys.REMINDER_ENABLED] = enabled
            p[SettingsKeys.REMINDER_MINUTES] = time.hour * 60 + time.minute
        }
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { p -> p[SettingsKeys.THEME] = theme.name }
    }
}
