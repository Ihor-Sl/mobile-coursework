package com.example.meditationapplication.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meditationapplication.R
import com.example.meditationapplication.data.model.SessionType
import com.example.meditationapplication.data.model.Stats
import com.example.meditationapplication.ui.AppViewModelProvider
import com.example.meditationapplication.ui.components.ScreenTopBar
import com.example.meditationapplication.ui.components.appCardBorder
import com.example.meditationapplication.ui.components.appCardColors
import com.example.meditationapplication.ui.components.minutesText
import com.example.meditationapplication.ui.components.sessionDateText
import com.example.meditationapplication.ui.components.ukPlural
import kotlinx.coroutines.flow.Flow

@Composable
fun StatsScreen(
    onGoHome: () -> Unit,
    scrollToTop: Flow<Unit>,
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    LaunchedEffect(scrollToTop) { scrollToTop.collect { listState.animateScrollToItem(0) } }

    Scaffold(topBar = { ScreenTopBar(title = stringResource(R.string.stats_title)) }) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { StatsRow(state.stats) }
            item {
                Text(
                    text = stringResource(R.string.stats_recent),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            if (state.recent.isEmpty() && !state.isLoading) {
                item { EmptyState(onGoHome) }
            } else {
                items(state.recent, key = { it.id }) { SessionRow(it) }
            }
        }
    }
}

@Composable
private fun StatsRow(stats: Stats) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            value = stats.totalSessions,
            label = ukPlural(stats.totalSessions, "сесія", "сесії", "сесій"),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = stats.totalMinutes,
            label = ukPlural(stats.totalMinutes, "хвилина", "хвилини", "хвилин"),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = stats.currentStreak,
            label = ukPlural(stats.currentStreak, "день поспіль", "дні поспіль", "днів поспіль"),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(value: Int, label: String, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier, colors = appCardColors(), border = appCardBorder()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SessionRow(item: SessionItem) {
    val isTimer = item.type == SessionType.TIMER
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = appCardColors(),
        border = appCardBorder(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isTimer) Icons.Rounded.Timer else Icons.Rounded.Headphones,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = when {
                        isTimer -> stringResource(R.string.session_timer)
                        else -> item.title ?: stringResource(R.string.session_unknown)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = sessionDateText(item.startedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = minutesText(item.durationSec),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(onGoHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.stats_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onGoHome) { Text(stringResource(R.string.go_home)) }
    }
}
