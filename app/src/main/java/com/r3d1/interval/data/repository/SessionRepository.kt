package com.r3d1.interval.data.repository

import com.r3d1.interval.data.db.SessionDao
import com.r3d1.interval.data.model.DailyStats
import com.r3d1.interval.data.model.Profile
import com.r3d1.interval.data.model.ProfileStats
import com.r3d1.interval.data.model.Session
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    fun getAllSessions(): Flow<List<Session>> = sessionDao.getAllSessions()

    fun getSessionsSince(startTime: Long): Flow<List<Session>> =
        sessionDao.getSessionsSince(startTime)

    suspend fun startSession(profile: Profile): Long {
        val session = Session(
            profileId = profile.id,
            profileName = profile.name,
            startTime = System.currentTimeMillis(),
            endTime = null,
            totalDurationSeconds = 0,
            plannedDurationMinutes = profile.totalSessionMinutes,
            intervalsCompleted = 0,
            intervalMinutes = profile.intervalMinutes,
            wasCompleted = false
        )
        return sessionDao.insertSession(session)
    }

    suspend fun updateSession(session: Session) = sessionDao.updateSession(session)

    suspend fun completeSession(
        sessionId: Long,
        durationSeconds: Int,
        intervalsCompleted: Int,
        wasCompleted: Boolean
    ) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        sessionDao.updateSession(
            session.copy(
                endTime = System.currentTimeMillis(),
                totalDurationSeconds = durationSeconds,
                intervalsCompleted = intervalsCompleted,
                wasCompleted = wasCompleted
            )
        )
    }

    // Statistics
    fun getDailyStats(daysBack: Int = 7): Flow<List<DailyStats>> {
        val startTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return sessionDao.getDailyStats(startTime)
    }

    fun getProfileStats(daysBack: Int = 30): Flow<List<ProfileStats>> {
        val startTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return sessionDao.getProfileStats(startTime)
    }

    fun getTotalMinutes(daysBack: Int = 7): Flow<Int?> {
        val startTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return sessionDao.getTotalMinutes(startTime)
    }

    fun getCompletedSessionsCount(daysBack: Int = 7): Flow<Int> {
        val startTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return sessionDao.getCompletedSessionsCount(startTime)
    }

    fun getActiveDaysCount(daysBack: Int = 7): Flow<Int> {
        val startTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return sessionDao.getActiveDaysCount(startTime)
    }

    suspend fun calculateCurrentStreak(): Int {
        val dates = sessionDao.getAllSessionDates()
        if (dates.isEmpty()) return 0

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        var streak = 0
        var currentDate = today

        for (dateStr in dates) {
            val sessionDate = LocalDate.parse(dateStr, formatter)
            when {
                sessionDate == currentDate -> {
                    streak++
                    currentDate = currentDate.minusDays(1)
                }
                sessionDate == currentDate.minusDays(1) && streak == 0 -> {
                    // Allow starting streak from yesterday if no session today
                    streak++
                    currentDate = sessionDate.minusDays(1)
                }
                else -> break
            }
        }
        return streak
    }

    suspend fun deleteOldSessions(keepDays: Int = 90) {
        val cutoffTime = System.currentTimeMillis() - (keepDays * 24 * 60 * 60 * 1000L)
        sessionDao.deleteSessionsBefore(cutoffTime)
    }
}
