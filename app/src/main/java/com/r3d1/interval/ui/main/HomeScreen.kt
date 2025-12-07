package com.r3d1.interval.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.r3d1.interval.data.model.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: (Long) -> Unit,
    onAddProfileClick: () -> Unit,
    onEditProfileClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "R3D1 Interval",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onStatisticsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = "Statistics")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProfileClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Profile")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Select a profile to start",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        onClick = { onProfileClick(profile.id) },
                        onLongClick = { onEditProfileClick(profile.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileCard(
    profile: Profile,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(profile.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(backgroundColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForProfile(profile.iconName),
                    contentDescription = null,
                    tint = backgroundColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${profile.intervalMinutes} min intervals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${profile.totalSessionMinutes} min total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (profile.breakAfterIntervals > 0) {
                    Text(
                        text = "Break every ${profile.breakAfterIntervals} intervals",
                        style = MaterialTheme.typography.bodySmall,
                        color = backgroundColor
                    )
                }
            }
        }
    }
}

fun getIconForProfile(iconName: String): ImageVector {
    return when (iconName) {
        "groups" -> Icons.Default.Groups
        "code" -> Icons.Default.Code
        "chat" -> Icons.Default.Chat
        "bolt" -> Icons.Default.Bolt
        "timer" -> Icons.Default.Timer
        "work" -> Icons.Default.Work
        "fitness" -> Icons.Default.FitnessCenter
        "book" -> Icons.Default.Book
        "music" -> Icons.Default.MusicNote
        "sports" -> Icons.Default.Sports
        else -> Icons.Default.Timer
    }
}
