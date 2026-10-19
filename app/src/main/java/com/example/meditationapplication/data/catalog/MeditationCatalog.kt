package com.example.meditationapplication.data.catalog

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Каталог медитацій з файлу assets/meditations.json.
 * Файл читається один раз при першому зверненні, далі список зберігається в пам'яті.
 */
class MeditationCatalog(
    private val assets: AssetManager,
    private val fileName: String = "meditations.json",
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cache: List<MeditationDto>? = null

    suspend fun load(): List<MeditationDto> = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) {
            val text = assets.open(fileName).bufferedReader().use { it.readText() }
            json.decodeFromString<List<MeditationDto>>(text)
                .filter { it.durationSec > 0 }
        }.also { cache = it }
    }
}
