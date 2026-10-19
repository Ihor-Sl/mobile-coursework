package com.example.meditationapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.meditationapplication.R
import com.example.meditationapplication.ui.theme.favorite

/** Верхня панель із заголовком екрана (headlineSmall) на кольорі фону. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
    }
}

/** Іконка ♡: заповнена кольором favorite, якщо медитація в обраному. */
@Composable
fun FavoriteButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onToggle, modifier = modifier) {
        if (isFavorite) {
            Icon(
                Icons.Rounded.Favorite,
                contentDescription = stringResource(R.string.remove_favorite),
                tint = MaterialTheme.colorScheme.favorite,
            )
        } else {
            Icon(
                Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(R.string.add_favorite),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Картки: білий фон (surface) і тонка рамка кольору outline. */
@Composable
fun appCardColors(): CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.surface,
)

@Composable
fun appCardBorder() = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

/** Діалог «Сесію завершено» — спільний для плеєра і таймера. */
@Composable
fun SessionCompletedDialog(
    minutes: Int,
    onDone: () -> Unit,
    onOpenStats: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDone,
        icon = {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(R.string.session_completed_title)) },
        text = { Text(stringResource(R.string.session_completed_text, minutes)) },
        confirmButton = {
            TextButton(onClick = onDone) { Text(stringResource(R.string.done)) }
        },
        dismissButton = {
            TextButton(onClick = onOpenStats) { Text(stringResource(R.string.statistics)) }
        },
    )
}
