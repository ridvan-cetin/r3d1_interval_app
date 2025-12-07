package com.r3d1.interval.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import androidx.core.app.NotificationCompat
import com.r3d1.interval.IntervalApp
import com.r3d1.interval.R
import com.r3d1.interval.data.model.Profile
import com.r3d1.interval.data.model.TimerState
import com.r3d1.interval.data.model.TimerStatus
import com.r3d1.interval.data.repository.PreferencesRepository
import com.r3d1.interval.data.repository.SessionRepository
import com.r3d1.interval.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var timerJob: Job? = null
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    private var currentSessionId: Long = 0
    private var soundEnabled = true
    private var vibrationEnabled = true

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        initToneGenerator()

        serviceScope.launch {
            preferencesRepository.soundEnabled.collect { soundEnabled = it }
        }
        serviceScope.launch {
            preferencesRepository.vibrationEnabled.collect { vibrationEnabled = it }
        }
    }

    private fun initToneGenerator() {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (e: Exception) {
            // ToneGenerator may fail on some devices
            toneGenerator = null
        }
    }

    override fun onBind(intent: Intent?): IBinder = TimerBinder()

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val profile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_PROFILE, Profile::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_PROFILE)
                }
                profile?.let { startTimer(it) }
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
            ACTION_SKIP_BREAK -> skipBreak()
        }
        return START_STICKY
    }

    fun startTimer(profile: Profile) {
        serviceScope.launch {
            currentSessionId = sessionRepository.startSession(profile)
            preferencesRepository.setLastProfileId(profile.id)

            val totalSeconds = profile.totalSessionMinutes * 60
            val intervalSeconds = profile.intervalMinutes * 60

            _timerState.value = TimerState(
                status = TimerStatus.RUNNING,
                currentProfile = profile,
                totalSessionSeconds = totalSeconds,
                remainingSessionSeconds = totalSeconds,
                currentIntervalSeconds = intervalSeconds,
                remainingIntervalSeconds = intervalSeconds,
                intervalsCompleted = 0,
                sessionStartTime = System.currentTimeMillis()
            )

            startForeground(NOTIFICATION_ID, createNotification())
            startTimerLoop()
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_timerState.value.status == TimerStatus.RUNNING ||
                   _timerState.value.status == TimerStatus.BREAK) {

                delay(1000)

                val current = _timerState.value
                if (current.status == TimerStatus.RUNNING) {
                    handleRunningTick(current)
                } else if (current.status == TimerStatus.BREAK) {
                    handleBreakTick(current)
                }

                updateNotification()
            }
        }
    }

    private suspend fun handleRunningTick(current: TimerState) {
        val newIntervalRemaining = current.remainingIntervalSeconds - 1
        val newSessionRemaining = current.remainingSessionSeconds - 1

        when {
            newSessionRemaining <= 0 -> {
                // Session completed
                completeSession(true)
            }
            newIntervalRemaining <= 0 -> {
                // Interval completed
                val newIntervalsCompleted = current.intervalsCompleted + 1
                playIntervalAlert()

                val profile = current.currentProfile!!
                val shouldBreak = profile.breakAfterIntervals > 0 &&
                        newIntervalsCompleted % profile.breakAfterIntervals == 0

                if (shouldBreak) {
                    _timerState.value = current.copy(
                        status = TimerStatus.BREAK,
                        isOnBreak = true,
                        breakRemainingSeconds = profile.breakDurationMinutes * 60,
                        intervalsCompleted = newIntervalsCompleted,
                        remainingIntervalSeconds = profile.intervalMinutes * 60,
                        remainingSessionSeconds = newSessionRemaining
                    )
                    showBreakNotification()
                } else {
                    _timerState.value = current.copy(
                        remainingIntervalSeconds = profile.intervalMinutes * 60,
                        remainingSessionSeconds = newSessionRemaining,
                        intervalsCompleted = newIntervalsCompleted
                    )
                }
            }
            else -> {
                _timerState.value = current.copy(
                    remainingIntervalSeconds = newIntervalRemaining,
                    remainingSessionSeconds = newSessionRemaining
                )
            }
        }
    }

    private fun handleBreakTick(current: TimerState) {
        val newBreakRemaining = current.breakRemainingSeconds - 1
        val newSessionRemaining = current.remainingSessionSeconds - 1

        if (newBreakRemaining <= 0 || newSessionRemaining <= 0) {
            if (newSessionRemaining <= 0) {
                completeSession(true)
            } else {
                // Break finished, resume timer
                playIntervalAlert()
                _timerState.value = current.copy(
                    status = TimerStatus.RUNNING,
                    isOnBreak = false,
                    breakRemainingSeconds = 0,
                    remainingSessionSeconds = newSessionRemaining
                )
            }
        } else {
            _timerState.value = current.copy(
                breakRemainingSeconds = newBreakRemaining,
                remainingSessionSeconds = newSessionRemaining
            )
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(status = TimerStatus.PAUSED)
        updateNotification()
    }

    fun resumeTimer() {
        _timerState.value = _timerState.value.copy(
            status = if (_timerState.value.isOnBreak) TimerStatus.BREAK else TimerStatus.RUNNING
        )
        startTimerLoop()
    }

    fun stopTimer() {
        timerJob?.cancel()
        val current = _timerState.value
        val elapsedSeconds = current.totalSessionSeconds - current.remainingSessionSeconds
        completeSession(false, elapsedSeconds)
    }

    private fun skipBreak() {
        val current = _timerState.value
        if (current.status == TimerStatus.BREAK) {
            _timerState.value = current.copy(
                status = TimerStatus.RUNNING,
                isOnBreak = false,
                breakRemainingSeconds = 0
            )
        }
    }

    private fun completeSession(completed: Boolean, elapsedSeconds: Int? = null) {
        timerJob?.cancel()
        val current = _timerState.value
        val duration = elapsedSeconds ?: (current.totalSessionSeconds - current.remainingSessionSeconds)

        serviceScope.launch {
            sessionRepository.completeSession(
                sessionId = currentSessionId,
                durationSeconds = duration,
                intervalsCompleted = current.intervalsCompleted,
                wasCompleted = completed
            )
        }

        _timerState.value = TimerState(status = TimerStatus.COMPLETED)
        if (completed) {
            showCompletionNotification()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun playIntervalAlert() {
        if (soundEnabled) {
            try {
                // Play a short beep tone
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            } catch (e: Exception) {
                // Fallback: try to reinitialize
                initToneGenerator()
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            }
        }

        if (vibrationEnabled) {
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 200, 100, 200),
                -1
            )
            vibrator?.vibrate(effect)
        }
    }

    private fun createNotification(): Notification {
        val state = _timerState.value
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = PendingIntent.getService(
            this, 1,
            Intent(this, TimerService::class.java).apply {
                action = if (state.status == TimerStatus.PAUSED) ACTION_RESUME else ACTION_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 2,
            Intent(this, TimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = NotificationCompat.Action(
            if (state.status == TimerStatus.PAUSED) R.drawable.ic_play else R.drawable.ic_pause,
            if (state.status == TimerStatus.PAUSED) "Resume" else "Pause",
            pauseResumeIntent
        )

        val stopAction = NotificationCompat.Action(
            R.drawable.ic_stop,
            "Stop",
            stopIntent
        )

        val statusText = when (state.status) {
            TimerStatus.RUNNING -> "Running"
            TimerStatus.PAUSED -> "Paused"
            TimerStatus.BREAK -> "Break time!"
            else -> ""
        }

        return NotificationCompat.Builder(this, IntervalApp.CHANNEL_TIMER)
            .setContentTitle("${state.currentProfile?.name ?: "Timer"} - $statusText")
            .setContentText("Session: ${state.formattedSessionTime} | Interval: ${state.formattedIntervalTime}")
            .setSmallIcon(R.drawable.ic_timer)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(pauseResumeAction)
            .addAction(stopAction)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun showBreakNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val skipIntent = PendingIntent.getService(
            this, 3,
            Intent(this, TimerService::class.java).apply { action = ACTION_SKIP_BREAK },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, IntervalApp.CHANNEL_BREAK)
            .setContentTitle("Time for a break!")
            .setContentText("Take ${_timerState.value.currentProfile?.breakDurationMinutes} minutes to rest")
            .setSmallIcon(R.drawable.ic_timer)
            .addAction(R.drawable.ic_skip, "Skip Break", skipIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(BREAK_NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val notification = NotificationCompat.Builder(this, IntervalApp.CHANNEL_ALERT)
            .setContentTitle("Session Complete!")
            .setContentText("Great job! You completed your ${_timerState.value.currentProfile?.name} session")
            .setSmallIcon(R.drawable.ic_timer)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(COMPLETE_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        toneGenerator?.release()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.r3d1.interval.START"
        const val ACTION_PAUSE = "com.r3d1.interval.PAUSE"
        const val ACTION_RESUME = "com.r3d1.interval.RESUME"
        const val ACTION_STOP = "com.r3d1.interval.STOP"
        const val ACTION_SKIP_BREAK = "com.r3d1.interval.SKIP_BREAK"
        const val EXTRA_PROFILE = "extra_profile"

        const val NOTIFICATION_ID = 1001
        const val BREAK_NOTIFICATION_ID = 1002
        const val COMPLETE_NOTIFICATION_ID = 1003
    }
}
