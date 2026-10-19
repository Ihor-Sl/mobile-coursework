package com.example.meditationapplication.ui.library

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditationapplication.R
import com.example.meditationapplication.data.model.Category
import com.example.meditationapplication.data.model.Meditation
import com.example.meditationapplication.data.repository.MeditationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Фільтри-чипи каталогу. Одночасно активний лише один. */
enum class LibraryFilter(@param:StringRes val labelRes: Int, val category: Category? = null) {
    ALL(R.string.filter_all),
    FAVORITES(R.string.filter_favorites),
    SLEEP(R.string.category_sleep, Category.SLEEP),
    STRESS(R.string.category_stress, Category.STRESS),
    FOCUS(R.string.category_focus, Category.FOCUS),
    BREATHING(R.string.category_breathing, Category.BREATHING),
}

data class LibraryUiState(
    val filter: LibraryFilter = LibraryFilter.ALL,
    val items: List<Meditation> = emptyList(),
    val isLoading: Boolean = true,
)

class LibraryViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val meditationRepository: MeditationRepository,
) : ViewModel() {

    private val filter = savedStateHandle.getStateFlow(FILTER_KEY, LibraryFilter.ALL.name)

    val uiState: StateFlow<LibraryUiState> = combine(
        meditationRepository.getAll(),
        meditationRepository.getFavorites(),
        filter,
    ) { all, favorites, filterName ->
        val current = LibraryFilter.entries.firstOrNull { it.name == filterName } ?: LibraryFilter.ALL
        LibraryUiState(filter = current, items = applyFilter(current, all, favorites), isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun setFilter(filter: LibraryFilter) {
        savedStateHandle[FILTER_KEY] = filter.name
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch { meditationRepository.toggleFavorite(id) }
    }

    companion object {
        /** Ключ у SavedStateHandle; через нього Головна відкриває каталог з фільтром «Обране». */
        const val FILTER_KEY = "filter"
    }
}

fun applyFilter(
    filter: LibraryFilter,
    all: List<Meditation>,
    favorites: List<Meditation>,
): List<Meditation> = when (filter) {
    LibraryFilter.ALL -> all
    LibraryFilter.FAVORITES -> favorites
    else -> all.filter { it.category == filter.category }
}
