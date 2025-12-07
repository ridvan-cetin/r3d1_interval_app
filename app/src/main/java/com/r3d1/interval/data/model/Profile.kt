package com.r3d1.interval.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val intervalMinutes: Int,
    val totalSessionMinutes: Int,
    val breakAfterIntervals: Int, // 0 means no break
    val breakDurationMinutes: Int = 5,
    val vibrationEnabled: Boolean = true,
    val isDefault: Boolean = false,
    val iconName: String = "timer", // Material icon name
    val colorHex: String = "#6200EE"
) : Parcelable {
    companion object {
        val DEFAULT_PROFILES = listOf(
            Profile(
                name = "Meeting",
                intervalMinutes = 15,
                totalSessionMinutes = 60,
                breakAfterIntervals = 0,
                vibrationEnabled = true,
                isDefault = true,
                iconName = "groups",
                colorHex = "#2196F3"
            ),
            Profile(
                name = "Coding",
                intervalMinutes = 25,
                totalSessionMinutes = 120,
                breakAfterIntervals = 4,
                breakDurationMinutes = 5,
                vibrationEnabled = true,
                isDefault = true,
                iconName = "code",
                colorHex = "#4CAF50"
            ),
            Profile(
                name = "Chit-chat",
                intervalMinutes = 10,
                totalSessionMinutes = 30,
                breakAfterIntervals = 0,
                vibrationEnabled = true,
                isDefault = true,
                iconName = "chat",
                colorHex = "#FF9800"
            ),
            Profile(
                name = "Quick Check",
                intervalMinutes = 1,
                totalSessionMinutes = 5,
                breakAfterIntervals = 0,
                vibrationEnabled = false,
                isDefault = true,
                iconName = "bolt",
                colorHex = "#E91E63"
            )
        )
    }
}
