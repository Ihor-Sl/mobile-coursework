package com.example.meditationapplication.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meditationapplication.R
import com.example.meditationapplication.data.model.Meditation
import com.example.meditationapplication.ui.AppViewModelProvider
import com.example.meditationapplication.ui.components.AssetImage
import com.example.meditationapplication.ui.components.ScreenTopBar
import com.example.meditationapplication.ui.components.appCardBorder
import com.example.meditationapplication.ui.components.appCardColors
import com.example.meditationapplication.ui.components.daysWord
import com.example.meditationapplication.ui.components.durationAndCategory
import com.example.meditationapplication.ui.components.minutesText
import com.example.meditationapplication.ui.theme.StreakFlame
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeScreen(
    onOpenMeditation: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavorites: () -> Unit,
    scrollToTop: Flow<Unit>,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    LaunchedEffect(scrollToTop) { scrollToTop.collect { listState.animateScrollToItem(0) } }

    val greeting = when (state.dayPart) {
        DayPart.MORNING -> R.string.greeting_morning
        DayPart.DAY -> R.string.greeting_day
        DayPart.EVENING -> R.string.greeting_evening
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = stringResource(greeting),
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.open_settings))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ProgressCard(streakDays = state.streakDays, minutesToday = state.minutesToday) }

            state.recommendation?.let { meditation ->
                item {
                    RecommendationCard(
                        meditation = meditation,
                        onStart = { onOpenMeditation(meditation.id) },
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.favorites),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onOpenFavorites) { Text(stringResource(R.string.see_all)) }
                }
            }

            item {
                if (state.favorites.isEmpty() && !state.isLoading) {
                    Text(
                        text = stringResource(R.string.favorites_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.favorites, key = { it.id }) { meditation ->
                            FavoriteCard(meditation = meditation, onClick = { onOpenMeditation(meditation.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(streakDays: Int, minutesToday: Int) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = appCardColors(),
        border = appCardBorder(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgressValue(
                icon = Icons.Rounded.LocalFireDepartment,
                iconTint = StreakFlame,
                value = "$streakDays ${daysWord(streakDays)}",
                label = stringResource(R.string.streak_label),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(color = MaterialTheme.colorScheme.outline)
            ProgressValue(
                icon = Icons.Rounded.Schedule,
                iconTint = MaterialTheme.colorScheme.primary,
                value = stringResource(R.string.minutes_short, minutesToday),
                label = stringResource(R.string.today_label),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProgressValue(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecommendationCard(meditation: Meditation, onStart: () -> Unit) {
    OutlinedCard(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth(),
        colors = appCardColors(),
        border = appCardBorder(),
    ) {
        AssetImage(
            path = meditation.imageFile,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        )
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.recommendation_of_day),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(meditation.title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = durationAndCategory(meditation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onStart, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.start))
            }
        }
    }
}

@Composable
private fun FavoriteCard(meditation: Meditation, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        colors = appCardColors(),
        border = appCardBorder(),
    ) {
        Column(Modifier.padding(8.dp)) {
            AssetImage(
                path = meditation.imageFile,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = meditation.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Text(
                text = minutesText(meditation.durationSec),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}
