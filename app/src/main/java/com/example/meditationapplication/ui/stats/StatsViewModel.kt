package com.example.meditationapplication.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditationapplication.data.model.SessionType
import com.example.meditationapplication.data.model.Stats
import com.example.meditationapplication.data.repository.MeditationRepository
import com.example.meditationapplication.data.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Рядок списку «Останні сесії». [title] = null — таймер або медитацію вилучено з каталогу. */
data class SessionItem(
    val id: Long,
    val type: SessionType,
    val title: String?,
    val startedAt: Long,
    val durationSec: Int,
)

data class StatsUiState(
    val stats: Stats = Stats(),
    val recent: List<SessionItem> = emptyList(),
    val isLoading: Boolean = true,
)

class StatsViewModel(
    sessionRepository: SessionRepository,
    meditationRepository: MeditationRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        sessionRepository.getStats(),
        sessionRepository.getRecent(RECENT_LIMIT),
        meditationRepository.getAll(),
    ) { stats, sessions, catalog ->
        // Назва медитації підставляється з каталогу за meditation_id
        val titles = catalog.associate { it.id to it.title }
        StatsUiState(
            stats = stats,
            recent = sessions.map { s ->
                SessionItem(
                    id = s.id,
                    type = s.type,
                    title = s.meditationId?.let(titles::get),
                    startedAt = s.startedAt,
                    durationSec = s.durationSec,
                )
            },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    companion object {
        const val RECENT_LIMIT = 20
    }
}
