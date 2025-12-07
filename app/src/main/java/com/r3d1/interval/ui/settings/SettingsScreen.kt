package com.r3d1.interval.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val soundEnabled by viewModel.soundEnabled.collectAsState(initial = true)
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState(initial = true)
    val alwaysOnDisplay by viewModel.alwaysOnDisplay.collectAsState(initial = false)
    val darkTheme by viewModel.darkTheme.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Timer Settings Section
            SettingsSection(title = "Timer") {
                SettingsSwitch(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound",
                    description = "Play beep when interval completes",
                    checked = soundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                )

                SettingsSwitch(
                    icon = Icons.Default.Vibration,
                    title = "Vibration",
                    description = "Vibrate when interval completes",
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Display Settings Section
            SettingsSection(title = "Display") {
                SettingsSwitch(
                    icon = Icons.Default.LightMode,
                    title = "Always On Display",
                    description = "Keep screen on during timer",
                    checked = alwaysOnDisplay,
                    onCheckedChange = { viewModel.setAlwaysOnDisplay(it) }
                )

                SettingsSwitch(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    description = "Use dark color scheme",
                    checked = darkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    description = "1.0.0"
                )

                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "R3D1 Interval Timer",
                    description = "Productivity interval timer app"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
