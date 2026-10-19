package com.example.meditationapplication.ui.timer

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meditationapplication.R
import com.example.meditationapplication.ui.AppViewModelProvider
import com.example.meditationapplication.ui.components.ScreenTopBar
import com.example.meditationapplication.ui.components.SessionCompletedDialog
import com.example.meditationapplication.ui.components.formatClock

@Composable
fun TimerScreen(
    onOpenStats: () -> Unit,
    viewModel: TimerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { ScreenTopBar(title = stringResource(R.string.timer_title)) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))
                TimerRing(state)
                Spacer(Modifier.height(32.dp))

                if (state.status == TimerStatus.IDLE) {
                    Text(
                        text = stringResource(R.string.duration),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TIMER_DURATIONS.forEach { minutes ->
                            FilterChip(
                                selected = minutes == state.selectedMinutes,
                                onClick = { viewModel.setDuration(minutes) },
                                label = {
                                    Text(
                                        text = minutes.toString(),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(24.dp),
                                    )
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.gong_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            TimerControls(
                status = state.status,
                onStart = viewModel::start,
                onPause = viewModel::pause,
                onStop = viewModel::stop,
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (state.status == TimerStatus.COMPLETED) {
        SessionCompletedDialog(
            minutes = state.selectedMinutes,
            onDone = viewModel::dismissCompleted,
            onOpenStats = {
                viewModel.dismissCompleted()
                onOpenStats()
            },
        )
    }
}

@Composable
private fun TimerRing(state: TimerUiState) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        CircularProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.size(220.dp),
            strokeWidth = 10.dp,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(formatClock(state.remainingMs), style = MaterialTheme.typography.displayMedium)
            Text(
                text = stringResource(R.string.timer_minutes_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimerControls(
    status: TimerStatus,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
) {
    val buttonModifier = Modifier.height(56.dp)
    when (status) {
        TimerStatus.IDLE, TimerStatus.COMPLETED -> Button(
            onClick = onStart,
            enabled = status == TimerStatus.IDLE,
            modifier = buttonModifier.fillMaxWidth(),
        ) {
            ButtonContent(Icons.Rounded.PlayArrow, stringResource(R.string.start))
        }
        TimerStatus.RUNNING, TimerStatus.PAUSED -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val running = status == TimerStatus.RUNNING
            Button(
                onClick = if (running) onPause else onStart,
                modifier = buttonModifier.weight(1f),
            ) {
                ButtonContent(
                    if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    stringResource(if (running) R.string.pause else R.string.resume),
                )
            }
            OutlinedButton(onClick = onStop, modifier = buttonModifier.weight(1f)) {
                ButtonContent(Icons.Rounded.Stop, stringResource(R.string.stop))
            }
        }
    }
}

@Composable
private fun ButtonContent(icon: ImageVector, text: String) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
    Text(text, style = MaterialTheme.typography.titleMedium)
}
