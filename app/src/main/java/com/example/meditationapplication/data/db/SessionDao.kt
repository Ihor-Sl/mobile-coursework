package com.example.meditationapplication.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.meditationapplication.data.model.MeditationSession
import com.example.meditationapplication.data.model.SessionType
import kotlinx.coroutines.flow.Flow

/** Запис таблиці sessions. Enum SessionType Room зберігає як TEXT (GUIDED / TIMER). */
@Entity(tableName = "sessions", indices = [Index("started_at")])
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: SessionType,
    @ColumnInfo(name = "meditation_id") val meditationId: String?,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "duration_sec") val durationSec: Int,
)

data class TotalsRow(val totalSessions: Int, val totalMinutes: Int)

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY started_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SessionEntity>>

    @Query(
        "SELECT COUNT(*) AS totalSessions, " +
            "COALESCE(SUM(duration_sec), 0) / 60 AS totalMinutes FROM sessions"
    )
    fun observeTotals(): Flow<TotalsRow>

    @Query(
        "SELECT COALESCE(SUM(duration_sec), 0) / 60 FROM sessions " +
            "WHERE started_at >= :fromMillis"
    )
    fun observeMinutesSince(fromMillis: Long): Flow<Int>

    @Query(
        "SELECT DISTINCT date(started_at / 1000, 'unixepoch', 'localtime') " +
            "AS day FROM sessions ORDER BY day DESC"
    )
    fun observeActiveDays(): Flow<List<String>>
}

fun SessionEntity.toModel() = MeditationSession(
    id = id,
    type = type,
    meditationId = meditationId,
    startedAt = startedAt,
    durationSec = durationSec,
)

fun MeditationSession.toEntity() = SessionEntity(
    id = id,
    type = type,
    meditationId = meditationId,
    startedAt = startedAt,
    durationSec = durationSec,
)
