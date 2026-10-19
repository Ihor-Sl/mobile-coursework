package com.example.meditationapplication.ui.timer

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditationapplication.data.model.MeditationSession
import com.example.meditationapplication.data.model.SessionType
import com.example.meditationapplication.data.repository.SessionRepository
import com.example.meditationapplication.service.AudioPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class TimerStatus { IDLE, RUNNING, PAUSED, COMPLETED }

data class TimerUiState(
    val selectedMinutes: Int = DEFAULT_MINUTES,
    val remainingMs: Long = DEFAULT_MINUTES * 60_000L,
    val status: TimerStatus = TimerStatus.IDLE,
) {
    val totalMs: Long get() = selectedMinutes * 60_000L
    /** Частка часу, що лишилася (для кільця, яке поступово зменшується). */
    val progress: Float get() = if (totalMs > 0) (remainingMs.toFloat() / totalMs).coerceIn(0f, 1f) else 0f
}

const val DEFAULT_MINUTES = 10
val TIMER_DURATIONS = listOf(5, 10, 15, 20, 30)

class TimerViewModel(
    private val sessionRepository: SessionRepository,
    private val gongPlayer: AudioPlayer,
    private val clock: () -> Long = System::currentTimeMillis,
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var startedAt = 0L

    fun setDuration(min: Int) {
        if (_uiState.value.status != TimerStatus.IDLE) return
        _uiState.value = TimerUiState(selectedMinutes = min, remainingMs = min * 60_000L)
    }

    /** Запуск таймера або продовження після паузи. */
    fun start() {
        val s = _uiState.value
        when (s.status) {
            TimerStatus.IDLE -> {
                startedAt = clock()
                playGong()
                runCountdown(s.totalMs)
            }
            TimerStatus.PAUSED -> runCountdown(s.remainingMs)
            else -> Unit
        }
    }

    fun pause() {
        if (_uiState.value.status != TimerStatus.RUNNING) return
        countdownJob?.cancel()
        _uiState.update { it.copy(status = TimerStatus.PAUSED) }
    }

    /** Дострокова зупинка без збереження сесії. */
    fun stop() {
        countdownJob?.cancel()
        reset()
    }

    /** Закриття діалогу «Сесію завершено». */
    fun dismissCompleted() {
        if (_uiState.value.status == TimerStatus.COMPLETED) reset()
    }

    private fun reset() {
        val minutes = _uiState.value.selectedMinutes
        _uiState.value = TimerUiState(selectedMinutes = minutes, remainingMs = minutes * 60_000L)
    }

    private fun runCountdown(remainingMs: Long) {
        countdownJob?.cancel()
        _uiState.update { it.copy(status = TimerStatus.RUNNING, remainingMs = remainingMs) }
        val endAt = elapsedRealtime() + remainingMs
        countdownJob = viewModelScope.launch {
            while (true) {
                val left = endAt - elapsedRealtime()
                if (left <= 0) break
                _uiState.update { it.copy(remainingMs = left) }
                delay(minOf(left, TICK_MS))
            }
            complete()
        }
    }

    private suspend fun complete() {
        playGong()
        val s = _uiState.value
        _uiState.update { it.copy(status = TimerStatus.COMPLETED, remainingMs = 0) }
        val session = MeditationSession(
            type = SessionType.TIMER,
            meditationId = null,
            startedAt = startedAt,
            // Таймер іде лише без пауз, тому фактична тривалість дорівнює обраній
            durationSec = s.selectedMinutes * 60,
        )
        withContext(NonCancellable) { sessionRepository.save(session) }
    }

    private fun playGong() = gongPlayer.playOnce(GONG_FILE)

    override fun onCleared() {
        countdownJob?.cancel()
        gongPlayer.release()
    }

    private companion object {
        const val TICK_MS = 200L
        const val GONG_FILE = "audio/gong.mp3"
    }
}
