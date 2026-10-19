package com.example.meditationapplication.ui.player

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditationapplication.data.model.Meditation
import com.example.meditationapplication.data.model.MeditationSession
import com.example.meditationapplication.data.model.SessionType
import com.example.meditationapplication.data.repository.MeditationRepository
import com.example.meditationapplication.data.repository.SessionRepository
import com.example.meditationapplication.service.AudioPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** «готовий до відтворення» → «відтворюється» ↔ «пауза» → «завершено». */
enum class PlaybackStatus { LOADING, NOT_FOUND, READY, PLAYING, PAUSED, COMPLETED }

data class PlayerUiState(
    val meditation: Meditation? = null,
    val status: PlaybackStatus = PlaybackStatus.LOADING,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    /** Фактично прослухано (без пауз) — заповнюється при завершенні. */
    val listenedSec: Int = 0,
) {
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

class PlayerViewModel(
    private val meditationRepository: MeditationRepository,
    private val sessionRepository: SessionRepository,
    private val audioPlayer: AudioPlayer,
    private val clock: () -> Long = System::currentTimeMillis,
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var loadedId: String? = null
    private var startedAt = 0L
    private var listenedMs = 0L
    private var tickerJob: Job? = null

    init {
        audioPlayer.onCompleted = ::onAudioCompleted
        // Синхронізація зі зміною стану плеєра ззовні (втрата аудіофокусу, від'єднання навушників)
        viewModelScope.launch {
            audioPlayer.isPlaying.collect { playing ->
                _uiState.update { s ->
                    when {
                        s.status == PlaybackStatus.PLAYING && !playing -> s.copy(status = PlaybackStatus.PAUSED)
                        s.status == PlaybackStatus.PAUSED && playing -> s.copy(status = PlaybackStatus.PLAYING)
                        else -> s
                    }
                }
            }
        }
    }

    fun load(id: String) {
        if (loadedId == id) return
        loadedId = id
        viewModelScope.launch {
            val meditation = meditationRepository.getById(id)
            if (meditation == null) {
                _uiState.value = PlayerUiState(status = PlaybackStatus.NOT_FOUND)
                return@launch
            }
            audioPlayer.prepare(meditation.audioFile)
            _uiState.value = PlayerUiState(
                meditation = meditation,
                status = PlaybackStatus.READY,
                durationMs = meditation.durationSec * 1000L,
            )
            // Стан «серця» оновлюється, якщо обране змінилося
            meditationRepository.getAll()
                .map { list -> list.firstOrNull { it.id == id }?.isFavorite ?: false }
                .distinctUntilChanged()
                .collect { isFavorite ->
                    _uiState.update { it.copy(meditation = it.meditation?.copy(isFavorite = isFavorite)) }
                }
        }
    }

    fun playPause() {
        when (_uiState.value.status) {
            PlaybackStatus.PLAYING -> {
                audioPlayer.pause()
                _uiState.update { it.copy(status = PlaybackStatus.PAUSED) }
            }
            PlaybackStatus.READY, PlaybackStatus.PAUSED -> {
                if (_uiState.value.status == PlaybackStatus.READY) {
                    startedAt = clock()
                    listenedMs = 0
                }
                audioPlayer.play()
                _uiState.update { it.copy(status = PlaybackStatus.PLAYING) }
                startTicker()
            }
            else -> Unit
        }
    }

    /** Дострокова зупинка без збереження сесії. */
    fun stop() {
        val status = _uiState.value.status
        if (status != PlaybackStatus.PLAYING && status != PlaybackStatus.PAUSED) return
        tickerJob?.cancel()
        audioPlayer.stop()
        listenedMs = 0
        _uiState.update { it.copy(status = PlaybackStatus.READY, positionMs = 0) }
    }

    fun seekTo(fraction: Float) {
        val s = _uiState.value
        if (s.status !in setOf(PlaybackStatus.READY, PlaybackStatus.PLAYING, PlaybackStatus.PAUSED)) return
        val position = (s.durationMs * fraction.coerceIn(0f, 1f)).toLong()
        audioPlayer.seekTo(position)
        _uiState.update { it.copy(positionMs = position) }
    }

    fun toggleFavorite() {
        val id = _uiState.value.meditation?.id ?: return
        viewModelScope.launch { meditationRepository.toggleFavorite(id) }
    }

    private fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            var last = elapsedRealtime()
            while (isActive) {
                delay(TICK_MS)
                val now = elapsedRealtime()
                if (audioPlayer.isPlaying.value) listenedMs += now - last
                last = now
                val duration = audioPlayer.durationMs.takeIf { it > 0 } ?: _uiState.value.durationMs
                _uiState.update {
                    it.copy(positionMs = audioPlayer.positionMs.coerceAtMost(duration), durationMs = duration)
                }
            }
        }
    }

    /** Аудіо дограло до кінця: зберігаємо сесію і показуємо діалог «Сесію завершено». */
    private fun onAudioCompleted() {
        val s = _uiState.value
        val meditation = s.meditation ?: return
        if (s.status != PlaybackStatus.PLAYING && s.status != PlaybackStatus.PAUSED) return
        tickerJob?.cancel()
        val durationSec = (listenedMs / 1000).toInt().coerceAtLeast(1)
        _uiState.update {
            it.copy(status = PlaybackStatus.COMPLETED, positionMs = it.durationMs, listenedSec = durationSec)
        }
        val session = MeditationSession(
            type = SessionType.GUIDED,
            meditationId = meditation.id,
            startedAt = startedAt,
            durationSec = durationSec,
        )
        viewModelScope.launch {
            // Запис не переривається, навіть якщо користувач одразу закриє екран
            withContext(NonCancellable) { sessionRepository.save(session) }
        }
    }

    override fun onCleared() {
        tickerJob?.cancel()
        audioPlayer.release()
    }

    private companion object {
        const val TICK_MS = 250L
    }
}
