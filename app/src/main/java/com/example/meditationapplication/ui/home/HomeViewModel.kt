package com.example.meditationapplication.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditationapplication.data.model.Meditation
import com.example.meditationapplication.data.repository.MeditationRepository
import com.example.meditationapplication.data.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class DayPart { MORNING, DAY, EVENING }

data class HomeUiState(
    val dayPart: DayPart = DayPart.DAY,
    val streakDays: Int = 0,
    val minutesToday: Int = 0,
    val recommendation: Meditation? = null,
    val favorites: List<Meditation> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeViewModel(
    meditationRepository: MeditationRepository,
    sessionRepository: SessionRepository,
    private val now: () -> LocalDateTime = LocalDateTime::now,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        meditationRepository.getAll(),
        meditationRepository.getFavorites(),
        sessionRepository.getStats(),
        sessionRepository.getMinutesToday(),
    ) { all, favorites, stats, minutesToday ->
        val current = now()
        HomeUiState(
            dayPart = dayPartOf(current.toLocalTime()),
            streakDays = stats.currentStreak,
            minutesToday = minutesToday,
            recommendation = recommendationFor(all, current.toLocalDate()),
            favorites = favorites,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(dayPart = dayPartOf(now().toLocalTime())),
    )
}

/** Привітання: ранок 05:00–11:59, день 12:00–17:59, інакше вечір. */
fun dayPartOf(time: LocalTime): DayPart = when (time.hour) {
    in 5..11 -> DayPart.MORNING
    in 12..17 -> DayPart.DAY
    else -> DayPart.EVENING
}

/** Рекомендація дня: змінюється щодня і обирається з каталогу за датою. */
fun recommendationFor(catalog: List<Meditation>, date: LocalDate): Meditation? {
    if (catalog.isEmpty()) return null
    return catalog[Math.floorMod(date.toEpochDay(), catalog.size.toLong()).toInt()]
}
