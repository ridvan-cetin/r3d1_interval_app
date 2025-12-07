package com.r3d1.interval.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r3d1.interval.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val soundEnabled: Flow<Boolean> = preferencesRepository.soundEnabled
    val vibrationEnabled: Flow<Boolean> = preferencesRepository.vibrationEnabled
    val alwaysOnDisplay: Flow<Boolean> = preferencesRepository.alwaysOnDisplay
    val darkTheme: Flow<Boolean> = preferencesRepository.darkTheme

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setVibrationEnabled(enabled)
        }
    }

    fun setAlwaysOnDisplay(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAlwaysOnDisplay(enabled)
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkTheme(enabled)
        }
    }
}
