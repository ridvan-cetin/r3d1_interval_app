package com.r3d1.interval

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class IntervalApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Timer notification channel (for foreground service)
        val timerChannel = NotificationChannel(
            CHANNEL_TIMER,
            "Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the current timer status"
            setShowBadge(false)
        }

        // Interval alert channel (for beeps/vibration)
        val alertChannel = NotificationChannel(
            CHANNEL_ALERT,
            "Interval Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when an interval completes"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200)
            // Use default notification sound
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            setSound(
                defaultSoundUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        // Break reminder channel
        val breakChannel = NotificationChannel(
            CHANNEL_BREAK,
            "Break Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminds you to take a break"
            enableVibration(true)
        }

        notificationManager.createNotificationChannels(
            listOf(timerChannel, alertChannel, breakChannel)
        )
    }

    companion object {
        const val CHANNEL_TIMER = "timer_channel"
        const val CHANNEL_ALERT = "alert_channel"
        const val CHANNEL_BREAK = "break_channel"
    }
}
