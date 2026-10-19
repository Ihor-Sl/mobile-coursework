package com.example.meditationapplication.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meditationapplication.R
import com.example.meditationapplication.data.model.Meditation
import com.example.meditationapplication.ui.AppViewModelProvider
import com.example.meditationapplication.ui.components.AssetImage
import com.example.meditationapplication.ui.components.BackButton
import com.example.meditationapplication.ui.components.FavoriteButton
import com.example.meditationapplication.ui.components.SessionCompletedDialog
import com.example.meditationapplication.ui.components.formatPosition
import com.example.meditationapplication.ui.components.labelRes
import com.example.meditationapplication.ui.components.minutesText
import com.example.meditationapplication.ui.components.secondsToMinutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    meditationId: String,
    onBack: () -> Unit,
    onOpenStats: () -> Unit,
    viewModel: PlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    LaunchedEffect(meditationId) { viewModel.load(meditationId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onBack) },
                actions = {
                    state.meditation?.let {
                        FavoriteButton(isFavorite = it.isFavorite, onToggle = viewModel::toggleFavorite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        val meditation = state.meditation
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.status == PlaybackStatus.NOT_FOUND -> Text(
                    stringResource(R.string.meditation_not_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                meditation == null -> CircularProgressIndicator()
                else -> PlayerContent(
                    meditation = meditation,
                    state = state,
                    onPlayPause = viewModel::playPause,
                    onStop = viewModel::stop,
                    onSeek = viewModel::seekTo,
                )
            }
        }
    }

    if (state.status == PlaybackStatus.COMPLETED) {
        SessionCompletedDialog(
            minutes = secondsToMinutes(state.listenedSec),
            onDone = onBack,
            onOpenStats = onOpenStats,
        )
    }
}

@Composable
private fun PlayerContent(
    meditation: Meditation,
    state: PlayerUiState,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Float) -> Unit,
) {
    // Позиція повзунка під час перетягування (до відпускання)
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val isActive = state.status == PlaybackStatus.PLAYING || state.status == PlaybackStatus.PAUSED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AssetImage(
            path = meditation.imageFile,
            contentDescription = null,
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(24.dp)),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = meditation.title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                R.string.meta_separator,
                stringResource(meditation.category.labelRes()),
                minutesText(meditation.durationSec),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        Slider(
            value = dragFraction ?: state.progress,
            onValueChange = { dragFraction = it },
            onValueChangeFinished = {
                dragFraction?.let(onSeek)
                dragFraction = null
            },
            enabled = state.status != PlaybackStatus.COMPLETED,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth()) {
            val shownPosition = dragFraction?.let { (it * state.durationMs).toLong() } ?: state.positionMs
            Text(
                formatPosition(shownPosition),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                formatPosition(state.durationMs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))

        // «Стоп» ліворуч, велика кнопка «Відтворити / Пауза» по центру
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedIconButton(
                onClick = onStop,
                enabled = isActive,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(Icons.Rounded.Stop, contentDescription = stringResource(R.string.stop))
            }
            Spacer(Modifier.width(24.dp))
            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(),
            ) {
                val playing = state.status == PlaybackStatus.PLAYING
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(if (playing) R.string.pause else R.string.play),
                    modifier = Modifier.size(36.dp),
                )
            }
            // Симетричний відступ, щоб кнопка відтворення була точно по центру
            Spacer(Modifier.width(24.dp + 56.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = meditation.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
