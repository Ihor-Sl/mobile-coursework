package com.example.meditationapplication.data.model

/** GUIDED — прослухана аудіомедитація, TIMER — самостійна медитація з таймером. */
enum class SessionType { GUIDED, TIMER }

/**
 * Завершена сесія медитації.
 * Для сесії таймера (type = TIMER) [meditationId] дорівнює null.
 */
data class MeditationSession(
    val id: Long = 0,
    val type: SessionType,
    val meditationId: String?,
    /** Час початку сесії, мс від 01.01.1970 (UTC). */
    val startedAt: Long,
    /** Фактична тривалість без урахування пауз, секунди (> 0). */
    val durationSec: Int,
)

/** Статистика не зберігається окремо, а обчислюється з таблиці sessions. */
data class Stats(
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
)
