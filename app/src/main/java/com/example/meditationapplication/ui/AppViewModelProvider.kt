package com.example.meditationapplication.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.meditationapplication.MeditationApp
import com.example.meditationapplication.ui.home.HomeViewModel
import com.example.meditationapplication.ui.library.LibraryViewModel
import com.example.meditationapplication.ui.player.PlayerViewModel
import com.example.meditationapplication.ui.settings.SettingsViewModel
import com.example.meditationapplication.ui.stats.StatsViewModel
import com.example.meditationapplication.ui.timer.TimerViewModel

/** Фабрика ViewModel: залежності беруться з AppContainer. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val c = meditationApp().container
            HomeViewModel(c.meditationRepository, c.sessionRepository)
        }
        initializer {
            LibraryViewModel(createSavedStateHandle(), meditationApp().container.meditationRepository)
        }
        initializer {
            val c = meditationApp().container
            PlayerViewModel(c.meditationRepository, c.sessionRepository, c.createAudioPlayer())
        }
        initializer {
            val c = meditationApp().container
            TimerViewModel(c.sessionRepository, c.createAudioPlayer(handleAudioFocus = false))
        }
        initializer {
            val c = meditationApp().container
            StatsViewModel(c.sessionRepository, c.meditationRepository)
        }
        initializer {
            val c = meditationApp().container
            SettingsViewModel(c.settingsRepository, c.reminderScheduler, c.appVersion)
        }
    }
}

private fun CreationExtras.meditationApp(): MeditationApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MeditationApp
