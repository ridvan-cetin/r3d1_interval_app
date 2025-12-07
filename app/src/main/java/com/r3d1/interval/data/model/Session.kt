package com.r3d1.interval.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("profileId")]
)
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long?,
    val profileName: String, // Store name in case profile is deleted
    val startTime: Long, // Unix timestamp
    val endTime: Long?, // Null if session is ongoing
    val totalDurationSeconds: Int, // Actual duration
    val plannedDurationMinutes: Int,
    val intervalsCompleted: Int,
    val intervalMinutes: Int,
    val wasCompleted: Boolean = false // Did user complete the full session?
)

data class DailyStats(
    val date: String, // Format: YYYY-MM-DD
    val totalSessions: Int,
    val totalMinutes: Int,
    val intervalsCompleted: Int
)

data class ProfileStats(
    val profileName: String,
    val totalSessions: Int,
    val totalMinutes: Int
)
