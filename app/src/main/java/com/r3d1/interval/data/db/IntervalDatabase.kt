package com.r3d1.interval.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.r3d1.interval.data.model.Profile
import com.r3d1.interval.data.model.Session

@Database(
    entities = [Profile::class, Session::class],
    version = 1,
    exportSchema = true
)
abstract class IntervalDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun sessionDao(): SessionDao
}
