package com.r3d1.interval.di

import android.content.Context
import androidx.room.Room
import com.r3d1.interval.data.db.IntervalDatabase
import com.r3d1.interval.data.db.ProfileDao
import com.r3d1.interval.data.db.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IntervalDatabase {
        return Room.databaseBuilder(
            context,
            IntervalDatabase::class.java,
            "interval_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(database: IntervalDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: IntervalDatabase): SessionDao {
        return database.sessionDao()
    }
}
