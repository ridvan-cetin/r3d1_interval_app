package com.r3d1.interval.data.db

import androidx.room.*
import com.r3d1.interval.data.model.DailyStats
import com.r3d1.interval.data.model.ProfileStats
import com.r3d1.interval.data.model.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime DESC")
    fun getSessionsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE startTime >= :startTime ORDER BY startTime DESC")
    fun getSessionsSince(startTime: Long): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): Session?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("DELETE FROM sessions WHERE startTime < :beforeTime")
    suspend fun deleteSessionsBefore(beforeTime: Long)

    // Statistics queries
    @Query("""
        SELECT
            date(startTime / 1000, 'unixepoch', 'localtime') as date,
            COUNT(*) as totalSessions,
            SUM(totalDurationSeconds) / 60 as totalMinutes,
            SUM(intervalsCompleted) as intervalsCompleted
        FROM sessions
        WHERE startTime >= :startTime
        GROUP BY date(startTime / 1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """)
    fun getDailyStats(startTime: Long): Flow<List<DailyStats>>

    @Query("""
        SELECT
            profileName,
            COUNT(*) as totalSessions,
            SUM(totalDurationSeconds) / 60 as totalMinutes
        FROM sessions
        WHERE startTime >= :startTime
        GROUP BY profileName
        ORDER BY totalMinutes DESC
    """)
    fun getProfileStats(startTime: Long): Flow<List<ProfileStats>>

    @Query("SELECT SUM(totalDurationSeconds) / 60 FROM sessions WHERE startTime >= :startTime")
    fun getTotalMinutes(startTime: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM sessions WHERE startTime >= :startTime AND wasCompleted = 1")
    fun getCompletedSessionsCount(startTime: Long): Flow<Int>

    @Query("""
        SELECT COUNT(DISTINCT date(startTime / 1000, 'unixepoch', 'localtime'))
        FROM sessions
        WHERE startTime >= :startTime
    """)
    fun getActiveDaysCount(startTime: Long): Flow<Int>

    // Streak calculation - days with at least one session
    @Query("""
        SELECT date(startTime / 1000, 'unixepoch', 'localtime') as date
        FROM sessions
        GROUP BY date(startTime / 1000, 'unixepoch', 'localtime')
        ORDER BY date DESC
    """)
    suspend fun getAllSessionDates(): List<String>
}
