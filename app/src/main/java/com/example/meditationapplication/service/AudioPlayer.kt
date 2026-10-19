package com.example.meditationapplication.service

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Обгортка над Media3 ExoPlayer для відтворення файлів з assets.
 * Використовується плеєром медитацій і таймером (звук гонгу).
 * Усі методи викликаються з головного потоку.
 */
class AudioPlayer(
    context: Context,
    handleAudioFocus: Boolean = true,
) {
    private val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            handleAudioFocus,
        )
        // Пауза, якщо від'єднали навушники
        setHandleAudioBecomingNoisy(handleAudioFocus)
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /** Викликається, коли файл дограв до кінця. */
    var onCompleted: (() -> Unit)? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onCompleted?.invoke()
            }
        })
    }

    /** Готує файл до відтворення; [assetPath] — шлях усередині assets, напр. "audio/gong.mp3". */
    fun prepare(assetPath: String) {
        player.setMediaItem(MediaItem.fromUri("asset:///$assetPath"))
        player.prepare()
    }

    fun play() {
        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
        player.play()
    }

    fun pause() = player.pause()

    /** Зупиняє відтворення і повертає позицію на початок (файл лишається підготовленим). */
    fun stop() {
        player.pause()
        player.seekTo(0)
    }

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    /** Програє файл один раз з початку (звук гонгу). */
    fun playOnce(assetPath: String) {
        prepare(assetPath)
        player.seekTo(0)
        player.play()
    }

    val positionMs: Long get() = player.currentPosition

    /** Тривалість файлу або 0, якщо ще невідома. */
    val durationMs: Long
        get() = player.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0L

    fun release() {
        onCompleted = null
        player.release()
    }
}
