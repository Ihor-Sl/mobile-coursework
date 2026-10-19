package com.example.meditationapplication

import android.content.Context
import com.example.meditationapplication.data.catalog.MeditationCatalog
import com.example.meditationapplication.data.db.AppDatabase
import com.example.meditationapplication.data.repository.DataStoreSettingsRepository
import com.example.meditationapplication.data.repository.MeditationRepository
import com.example.meditationapplication.data.repository.OfflineMeditationRepository
import com.example.meditationapplication.data.repository.RoomSessionRepository
import com.example.meditationapplication.data.repository.SessionRepository
import com.example.meditationapplication.data.repository.SettingsRepository
import com.example.meditationapplication.service.AudioPlayer
import com.example.meditationapplication.service.ReminderScheduler

/** Створює залежності додатка. Екземпляр зберігається в [MeditationApp]. */
class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy { AppDatabase.create(context) }

    val meditationRepository: MeditationRepository by lazy {
        OfflineMeditationRepository(MeditationCatalog(context.assets), database.favoriteDao())
    }

    val sessionRepository: SessionRepository by lazy {
        RoomSessionRepository(database.sessionDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(context)
    }

    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(context) }

    /** Кожна ViewModel отримує власний плеєр і звільняє його в onCleared(). */
    fun createAudioPlayer(handleAudioFocus: Boolean = true) = AudioPlayer(context, handleAudioFocus)

    val appVersion: String by lazy {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "1.0"
    }
}
