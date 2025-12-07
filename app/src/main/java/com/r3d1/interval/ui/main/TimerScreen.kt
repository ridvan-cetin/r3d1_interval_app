package com.r3d1.interval.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.r3d1.interval.data.model.Profile
import com.r3d1.interval.data.model.TimerState
import com.r3d1.interval.data.model.TimerStatus
import com.r3d1.interval.service.TimerService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    profileId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val timerState by viewModel.timerState.collectAsState()

    var timerService by remember { mutableStateOf<TimerService?>(null) }
    var isBound by remember { mutableStateOf(false) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as TimerService.TimerBinder
                timerService = binder.getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                timerService = null
                isBound = false
            }
        }
    }

    // Bind to service when screen is shown
    LaunchedEffect(Unit) {
        viewModel.loadProfile(profileId)
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // Collect timer state from service
    LaunchedEffect(timerService) {
        timerService?.timerState?.collect { state ->
            viewModel.updateTimerState(state)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isBound) {
                context.unbindService(connection)
            }
        }
    }

    val profileColor = profile?.let {
        try {
            Color(android.graphics.Color.parseColor(it.colorHex))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } ?: MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(profile?.name ?: "Timer") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (timerState.status == TimerStatus.IDLE ||
                            timerState.status == TimerStatus.COMPLETED) {
                            onNavigateBack()
                        }
                    }) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status indicator
            StatusBadge(
                status = timerState.status,
                isOnBreak = timerState.isOnBreak,
                color = profileColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Timer display
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TimerDisplay(
                    timerState = timerState,
                    profileColor = profileColor
                )
            }

            // Intervals completed
            Text(
                text = "Intervals: ${timerState.intervalsCompleted}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Session time remaining
            Text(
                text = "Session: ${timerState.formattedSessionTime}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Control buttons
            TimerControls(
                timerState = timerState,
                profileColor = profileColor,
                onStart = {
                    profile?.let { p ->
                        val intent = Intent(context, TimerService::class.java).apply {
                            action = TimerService.ACTION_START
                            putExtra(TimerService.EXTRA_PROFILE, p)
                        }
                        context.startForegroundService(intent)
                    }
                },
                onPause = {
                    val intent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_PAUSE
                    }
                    context.startService(intent)
                },
                onResume = {
                    val intent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_RESUME
                    }
                    context.startService(intent)
                },
                onStop = {
                    val intent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_STOP
                    }
                    context.startService(intent)
                },
                onSkipBreak = {
                    val intent = Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_SKIP_BREAK
                    }
                    context.startService(intent)
                },
                onDone = onNavigateBack
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: TimerStatus,
    isOnBreak: Boolean,
    color: Color
) {
    val (text, badgeColor) = when {
        isOnBreak -> "BREAK TIME" to Color(0xFFFF9800)
        status == TimerStatus.RUNNING -> "RUNNING" to color
        status == TimerStatus.PAUSED -> "PAUSED" to Color(0xFFFFEB3B)
        status == TimerStatus.COMPLETED -> "COMPLETED" to Color(0xFF4CAF50)
        else -> "READY" to MaterialTheme.colorScheme.outline
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = badgeColor.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = badgeColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimerDisplay(
    timerState: TimerState,
    profileColor: Color
) {
    val progress = if (timerState.isOnBreak) {
        val totalBreak = (timerState.currentProfile?.breakDurationMinutes ?: 5) * 60f
        1f - (timerState.breakRemainingSeconds / totalBreak)
    } else {
        timerState.intervalProgress
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        // Background circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = profileColor.copy(alpha = 0.1f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Progress arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = if (timerState.isOnBreak) Color(0xFFFF9800) else profileColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Time text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (timerState.isOnBreak) "Break" else "Interval",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = timerState.formattedIntervalTime,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 64.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TimerControls(
    timerState: TimerState,
    profileColor: Color,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkipBreak: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (timerState.status) {
            TimerStatus.IDLE -> {
                FilledIconButton(
                    onClick = onStart,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = profileColor
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            TimerStatus.RUNNING -> {
                OutlinedIconButton(
                    onClick = onStop,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }

                FilledIconButton(
                    onClick = onPause,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = profileColor
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            TimerStatus.PAUSED -> {
                OutlinedIconButton(
                    onClick = onStop,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }

                FilledIconButton(
                    onClick = onResume,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = profileColor
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            TimerStatus.BREAK -> {
                OutlinedIconButton(
                    onClick = onStop,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }

                FilledIconButton(
                    onClick = onSkipBreak,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Skip Break",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            TimerStatus.COMPLETED -> {
                FilledIconButton(
                    onClick = onDone,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Done",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}
