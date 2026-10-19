package com.example.meditationapplication.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditationapplication.data.model.AppTheme
import com.example.meditationapplication.data.model.UserSettings
import com.example.meditationapplication.data.repository.SettingsRepository
import com.example.meditationapplication.service.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val appVersion: String = "",
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    appVersion: String,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.settings
        .map { SettingsUiState(settings = it, appVersion = appVersion) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState(appVersion = appVersion))

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val time = settingsRepository.settings.first().reminderTime
            updateReminder(enabled, time)
        }
    }

    fun setReminderTime(time: LocalTime) {
        viewModelScope.launch {
            val enabled = settingsRepository.settings.first().reminderEnabled
            updateReminder(enabled, time)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    /** Зберігаємо значення, після чого переплановуємо або скасовуємо щоденне завдання. */
    private suspend fun updateReminder(enabled: Boolean, time: LocalTime) {
        settingsRepository.setReminder(enabled, time)
        if (enabled) reminderScheduler.schedule(time) else reminderScheduler.cancel()
    }
}
