package com.r3d1.interval.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Keys
    private object Keys {
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val ALWAYS_ON_DISPLAY = booleanPreferencesKey("always_on_display")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val LAST_PROFILE_ID = longPreferencesKey("last_profile_id")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SHOW_NOTIFICATIONS = booleanPreferencesKey("show_notifications")
    }

    // Vibration
    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.VIBRATION_ENABLED] ?: true
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.VIBRATION_ENABLED] = enabled
        }
    }

    // Always On Display
    val alwaysOnDisplay: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ALWAYS_ON_DISPLAY] ?: false
    }

    suspend fun setAlwaysOnDisplay(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ALWAYS_ON_DISPLAY] = enabled
        }
    }

    // Sound
    val soundEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.SOUND_ENABLED] ?: true
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    // Last used profile
    val lastProfileId: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_PROFILE_ID]
    }

    suspend fun setLastProfileId(id: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_PROFILE_ID] = id
        }
    }

    // Dark theme
    val darkTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DARK_THEME] ?: true // Default to dark theme
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = enabled
        }
    }

    // Notifications
    val showNotifications: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.SHOW_NOTIFICATIONS] ?: true
    }

    suspend fun setShowNotifications(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.SHOW_NOTIFICATIONS] = enabled
        }
    }
}
