package com.r3d1.interval.data.model

data class TimerState(
    val status: TimerStatus = TimerStatus.IDLE,
    val currentProfile: Profile? = null,
    val totalSessionSeconds: Int = 0,
    val remainingSessionSeconds: Int = 0,
    val currentIntervalSeconds: Int = 0,
    val remainingIntervalSeconds: Int = 0,
    val intervalsCompleted: Int = 0,
    val isOnBreak: Boolean = false,
    val breakRemainingSeconds: Int = 0,
    val sessionStartTime: Long = 0
) {
    val sessionProgress: Float
        get() = if (totalSessionSeconds > 0) {
            1f - (remainingSessionSeconds.toFloat() / totalSessionSeconds)
        } else 0f

    val intervalProgress: Float
        get() = if (currentIntervalSeconds > 0) {
            1f - (remainingIntervalSeconds.toFloat() / currentIntervalSeconds)
        } else 0f

    val formattedSessionTime: String
        get() = formatTime(remainingSessionSeconds)

    val formattedIntervalTime: String
        get() = formatTime(if (isOnBreak) breakRemainingSeconds else remainingIntervalSeconds)

    val formattedElapsedTime: String
        get() = formatTime(totalSessionSeconds - remainingSessionSeconds)

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
}

enum class TimerStatus {
    IDLE,       // No timer running
    RUNNING,    // Timer is active
    PAUSED,     // Timer is paused
    BREAK,      // On a break between intervals
    COMPLETED   // Session finished
}
