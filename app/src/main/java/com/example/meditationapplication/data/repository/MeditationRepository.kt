package com.example.meditationapplication.data.repository

import com.example.meditationapplication.data.catalog.MeditationCatalog
import com.example.meditationapplication.data.catalog.toMeditation
import com.example.meditationapplication.data.db.FavoriteDao
import com.example.meditationapplication.data.db.FavoriteEntity
import com.example.meditationapplication.data.model.Meditation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/** Каталог медитацій + обране. */
interface MeditationRepository {
    /** Увесь каталог у порядку файлу; isFavorite оновлюється при зміні обраного. */
    fun getAll(): Flow<List<Meditation>>

    /** Обрані медитації, нові зверху. Ідентифікатори, яких немає в каталозі, ігноруються. */
    fun getFavorites(): Flow<List<Meditation>>

    suspend fun getById(id: String): Meditation?

    suspend fun toggleFavorite(id: String)
}

class OfflineMeditationRepository(
    private val catalog: MeditationCatalog,
    private val favoriteDao: FavoriteDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : MeditationRepository {

    private val catalogFlow = flow { emit(catalog.load()) }

    override fun getAll(): Flow<List<Meditation>> =
        combine(catalogFlow, favoriteDao.observeIds()) { items, favoriteIds ->
            val ids = favoriteIds.toSet()
            items.map { it.toMeditation(isFavorite = it.id in ids) }
        }

    override fun getFavorites(): Flow<List<Meditation>> =
        combine(catalogFlow, favoriteDao.observeIds()) { items, favoriteIds ->
            val byId = items.associateBy { it.id }
            favoriteIds.mapNotNull { id -> byId[id]?.toMeditation(isFavorite = true) }
        }

    override suspend fun getById(id: String): Meditation? {
        val dto = catalog.load().firstOrNull { it.id == id } ?: return null
        return dto.toMeditation(isFavorite = id in favoriteDao.observeIds().first())
    }

    override suspend fun toggleFavorite(id: String) {
        if (id in favoriteDao.observeIds().first()) {
            favoriteDao.remove(id)
        } else {
            favoriteDao.add(FavoriteEntity(meditationId = id, addedAt = clock()))
        }
    }
}
