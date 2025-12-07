package com.r3d1.interval.data.repository

import com.r3d1.interval.data.db.ProfileDao
import com.r3d1.interval.data.model.Profile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    fun getAllProfiles(): Flow<List<Profile>> = profileDao.getAllProfiles()

    suspend fun getProfileById(id: Long): Profile? = profileDao.getProfileById(id)

    suspend fun getProfileByName(name: String): Profile? = profileDao.getProfileByName(name)

    suspend fun insertProfile(profile: Profile): Long = profileDao.insertProfile(profile)

    suspend fun updateProfile(profile: Profile) = profileDao.updateProfile(profile)

    suspend fun deleteProfile(profile: Profile) = profileDao.deleteProfile(profile)

    suspend fun initializeDefaultProfiles() {
        val count = profileDao.getProfileCount()
        if (count == 0) {
            profileDao.insertProfiles(Profile.DEFAULT_PROFILES)
        }
    }

    suspend fun createCustomProfile(
        name: String,
        intervalMinutes: Int,
        totalSessionMinutes: Int,
        breakAfterIntervals: Int = 0,
        breakDurationMinutes: Int = 5,
        vibrationEnabled: Boolean = true,
        iconName: String = "timer",
        colorHex: String = "#6200EE"
    ): Long {
        val profile = Profile(
            name = name,
            intervalMinutes = intervalMinutes,
            totalSessionMinutes = totalSessionMinutes,
            breakAfterIntervals = breakAfterIntervals,
            breakDurationMinutes = breakDurationMinutes,
            vibrationEnabled = vibrationEnabled,
            isDefault = false,
            iconName = iconName,
            colorHex = colorHex
        )
        return profileDao.insertProfile(profile)
    }
}
