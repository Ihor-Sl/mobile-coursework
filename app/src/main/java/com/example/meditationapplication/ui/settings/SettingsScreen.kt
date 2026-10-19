package com.example.meditationapplication.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meditationapplication.R
import com.example.meditationapplication.data.model.AppTheme
import com.example.meditationapplication.ui.AppViewModelProvider
import com.example.meditationapplication.ui.components.BackButton
import com.example.meditationapplication.ui.components.ScreenTopBar
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = state.settings
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }

    // На Android 13+ для сповіщень потрібен дозвіл POST_NOTIFICATIONS
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
        } else {
            Toast.makeText(context, R.string.notifications_denied, Toast.LENGTH_LONG).show()
        }
    }
    val onReminderToggle: (Boolean) -> Unit = { enabled ->
        val needsPermission = enabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.setReminderEnabled(enabled)
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.settings_title),
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            SectionTitle(stringResource(R.string.section_reminders))
            ListItem(
                headlineContent = { Text(stringResource(R.string.daily_reminder)) },
                supportingContent = { Text(stringResource(R.string.daily_reminder_hint)) },
                trailingContent = { Switch(checked = settings.reminderEnabled, onCheckedChange = null) },
                colors = transparentListItem(),
                modifier = Modifier.toggleable(
                    value = settings.reminderEnabled,
                    role = Role.Switch,
                    onValueChange = onReminderToggle,
                ),
            )
            SettingsDivider()
            // Пункт неактивний, якщо нагадування вимкнено
            ListItem(
                headlineContent = { Text(stringResource(R.string.reminder_time)) },
                trailingContent = {
                    Text(
                        text = settings.reminderTime.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                colors = transparentListItem(),
                modifier = Modifier
                    .clickable(enabled = settings.reminderEnabled) { showTimePicker = true }
                    .alpha(if (settings.reminderEnabled) 1f else 0.38f),
            )
            SettingsDivider()

            SectionTitle(stringResource(R.string.section_appearance))
            Column(Modifier.selectableGroup()) {
                ThemeOption(AppTheme.SYSTEM, R.string.theme_system, settings.theme, viewModel::setTheme)
                ThemeOption(AppTheme.LIGHT, R.string.theme_light, settings.theme, viewModel::setTheme)
                ThemeOption(AppTheme.DARK, R.string.theme_dark, settings.theme, viewModel::setTheme)
            }
            SettingsDivider()

            SectionTitle(stringResource(R.string.section_about))
            ListItem(
                headlineContent = { Text(stringResource(R.string.version)) },
                trailingContent = { Text(state.appVersion, style = MaterialTheme.typography.bodyLarge) },
                colors = transparentListItem(),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showTimePicker) {
        ReminderTimeDialog(
            initial = settings.reminderTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                showTimePicker = false
                viewModel.setReminderTime(time)
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsDivider() = HorizontalDivider(color = MaterialTheme.colorScheme.outline)

@Composable
private fun transparentListItem() = ListItemDefaults.colors(containerColor = Color.Transparent)

@Composable
private fun ThemeOption(
    theme: AppTheme,
    labelRes: Int,
    selected: AppTheme,
    onSelect: (AppTheme) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = theme == selected,
                onClick = { onSelect(theme) },
                role = Role.RadioButton,
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = theme == selected, onClick = null, modifier = Modifier.padding(start = 12.dp))
        Spacer(Modifier.width(16.dp))
        Text(stringResource(labelRes), style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_time)) },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
