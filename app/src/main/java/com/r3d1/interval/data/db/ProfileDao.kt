package com.r3d1.interval.data.db

import androidx.room.*
import com.r3d1.interval.data.model.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY isDefault DESC, name ASC")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): Profile?

    @Query("SELECT * FROM profiles WHERE name = :name LIMIT 1")
    suspend fun getProfileByName(name: String): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<Profile>)

    @Update
    suspend fun updateProfile(profile: Profile)

    @Delete
    suspend fun deleteProfile(profile: Profile)

    @Query("DELETE FROM profiles WHERE isDefault = 0")
    suspend fun deleteCustomProfiles()

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
}
