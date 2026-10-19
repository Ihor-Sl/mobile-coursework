package com.example.meditationapplication.data.repository

import com.example.meditationapplication.data.db.SessionDao
import com.example.meditationapplication.data.db.toEntity
import com.example.meditationapplication.data.db.toModel
import com.example.meditationapplication.data.model.MeditationSession
import com.example.meditationapplication.data.model.Stats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/** Історія сесій та статистика. */
interface SessionRepository {
    suspend fun save(session: MeditationSession)

    fun getRecent(limit: Int): Flow<List<MeditationSession>>

    fun getStats(): Flow<Stats>

    /** Хвилини медитації за поточну добу (для головного екрана). */
    fun getMinutesToday(): Flow<Int>
}

class RoomSessionRepository(
    private val sessionDao: SessionDao,
    private val today: () -> LocalDate = LocalDate::now,
    private val zone: () -> ZoneId = ZoneId::systemDefault,
) : SessionRepository {

    override suspend fun save(session: MeditationSession) {
        require(session.durationSec > 0) { "durationSec must be > 0" }
        sessionDao.insert(session.toEntity())
    }

    override fun getRecent(limit: Int): Flow<List<MeditationSession>> =
        sessionDao.observeRecent(limit).map { list -> list.map { it.toModel() } }

    override fun getStats(): Flow<Stats> =
        combine(sessionDao.observeTotals(), sessionDao.observeActiveDays()) { totals, days ->
            Stats(
                totalSessions = totals.totalSessions,
                totalMinutes = totals.totalMinutes,
                currentStreak = calculateStreak(days.map(LocalDate::parse), today()),
            )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMinutesToday(): Flow<Int> =
        flow { emit(today().atStartOfDay(zone()).toInstant().toEpochMilli()) }
            .flatMapLatest { startOfDay -> sessionDao.observeMinutesSince(startOfDay) }
}

/**
 * Серія днів поспіль: дні з сесіями перебираються від сьогодні назад
 * (або від учора, якщо сьогодні сесій ще не було), доки йдуть без пропусків.
 */
fun calculateStreak(activeDays: Collection<LocalDate>, today: LocalDate): Int {
    val days = activeDays.toSet()
    var day = if (today in days) today else today.minusDays(1)
    var streak = 0
    while (day in days) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}
