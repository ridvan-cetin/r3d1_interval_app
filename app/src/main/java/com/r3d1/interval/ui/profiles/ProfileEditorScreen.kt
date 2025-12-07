package com.r3d1.interval.ui.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.r3d1.interval.ui.main.getIconForProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    profileId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: ProfileEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = profileId != null

    LaunchedEffect(profileId) {
        profileId?.let { viewModel.loadProfile(it) }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Profile" else "New Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    if (isEditing && !uiState.isDefault) {
                        IconButton(onClick = { viewModel.deleteProfile() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    TextButton(
                        onClick = { viewModel.saveProfile() },
                        enabled = uiState.name.isNotBlank()
                    ) {
                        Text("Save")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Profile Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Interval duration
            SliderSection(
                title = "Interval Duration",
                value = uiState.intervalMinutes,
                valueText = "${uiState.intervalMinutes} minutes",
                range = 1f..60f,
                steps = 58,
                onValueChange = { viewModel.updateIntervalMinutes(it.toInt()) }
            )

            // Total session duration
            SliderSection(
                title = "Total Session Duration",
                value = uiState.totalSessionMinutes,
                valueText = "${uiState.totalSessionMinutes} minutes",
                range = 5f..240f,
                steps = 46,
                onValueChange = { viewModel.updateTotalSessionMinutes(it.toInt()) }
            )

            // Break settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Break Reminders",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = uiState.breakAfterIntervals > 0,
                            onCheckedChange = {
                                viewModel.updateBreakAfterIntervals(if (it) 4 else 0)
                            }
                        )
                    }

                    if (uiState.breakAfterIntervals > 0) {
                        Spacer(modifier = Modifier.height(16.dp))

                        SliderSection(
                            title = "Break after intervals",
                            value = uiState.breakAfterIntervals,
                            valueText = "${uiState.breakAfterIntervals} intervals",
                            range = 1f..10f,
                            steps = 8,
                            onValueChange = { viewModel.updateBreakAfterIntervals(it.toInt()) }
                        )

                        SliderSection(
                            title = "Break duration",
                            value = uiState.breakDurationMinutes,
                            valueText = "${uiState.breakDurationMinutes} minutes",
                            range = 1f..15f,
                            steps = 13,
                            onValueChange = { viewModel.updateBreakDurationMinutes(it.toInt()) }
                        )
                    }
                }
            }

            // Vibration toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vibration",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = uiState.vibrationEnabled,
                    onCheckedChange = { viewModel.updateVibrationEnabled(it) }
                )
            }

            // Icon selection
            Text(
                text = "Icon",
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableIcons) { iconName ->
                    IconOption(
                        iconName = iconName,
                        isSelected = uiState.iconName == iconName,
                        color = try {
                            Color(android.graphics.Color.parseColor(uiState.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        },
                        onClick = { viewModel.updateIconName(iconName) }
                    )
                }
            }

            // Color selection
            Text(
                text = "Color",
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableColors) { colorHex ->
                    ColorOption(
                        colorHex = colorHex,
                        isSelected = uiState.colorHex == colorHex,
                        onClick = { viewModel.updateColorHex(colorHex) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SliderSection(
    title: String,
    value: Int,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps
        )
    }
}

@Composable
fun IconOption(
    iconName: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getIconForProfile(iconName),
            contentDescription = iconName,
            tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ColorOption(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White
            )
        }
    }
}

val availableIcons = listOf(
    "timer", "code", "groups", "chat", "bolt",
    "work", "fitness", "book", "music", "sports"
)

val availableColors = listOf(
    "#2196F3", "#4CAF50", "#FF9800", "#E91E63",
    "#9C27B0", "#00BCD4", "#FF5722", "#795548",
    "#607D8B", "#3F51B5"
)
