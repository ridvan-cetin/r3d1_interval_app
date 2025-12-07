package com.r3d1.interval.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r3d1.interval.data.model.Profile
import com.r3d1.interval.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileEditorUiState(
    val id: Long = 0,
    val name: String = "",
    val intervalMinutes: Int = 25,
    val totalSessionMinutes: Int = 60,
    val breakAfterIntervals: Int = 0,
    val breakDurationMinutes: Int = 5,
    val vibrationEnabled: Boolean = true,
    val isDefault: Boolean = false,
    val iconName: String = "timer",
    val colorHex: String = "#6200EE",
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class ProfileEditorViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditorUiState())
    val uiState: StateFlow<ProfileEditorUiState> = _uiState.asStateFlow()

    fun loadProfile(profileId: Long) {
        viewModelScope.launch {
            profileRepository.getProfileById(profileId)?.let { profile ->
                _uiState.value = ProfileEditorUiState(
                    id = profile.id,
                    name = profile.name,
                    intervalMinutes = profile.intervalMinutes,
                    totalSessionMinutes = profile.totalSessionMinutes,
                    breakAfterIntervals = profile.breakAfterIntervals,
                    breakDurationMinutes = profile.breakDurationMinutes,
                    vibrationEnabled = profile.vibrationEnabled,
                    isDefault = profile.isDefault,
                    iconName = profile.iconName,
                    colorHex = profile.colorHex
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateIntervalMinutes(minutes: Int) {
        _uiState.update { it.copy(intervalMinutes = minutes) }
    }

    fun updateTotalSessionMinutes(minutes: Int) {
        _uiState.update { it.copy(totalSessionMinutes = minutes) }
    }

    fun updateBreakAfterIntervals(intervals: Int) {
        _uiState.update { it.copy(breakAfterIntervals = intervals) }
    }

    fun updateBreakDurationMinutes(minutes: Int) {
        _uiState.update { it.copy(breakDurationMinutes = minutes) }
    }

    fun updateVibrationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun updateIconName(iconName: String) {
        _uiState.update { it.copy(iconName = iconName) }
    }

    fun updateColorHex(colorHex: String) {
        _uiState.update { it.copy(colorHex = colorHex) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val profile = Profile(
                id = state.id,
                name = state.name,
                intervalMinutes = state.intervalMinutes,
                totalSessionMinutes = state.totalSessionMinutes,
                breakAfterIntervals = state.breakAfterIntervals,
                breakDurationMinutes = state.breakDurationMinutes,
                vibrationEnabled = state.vibrationEnabled,
                isDefault = state.isDefault,
                iconName = state.iconName,
                colorHex = state.colorHex
            )

            if (state.id == 0L) {
                profileRepository.insertProfile(profile)
            } else {
                profileRepository.updateProfile(profile)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.id != 0L && !state.isDefault) {
                val profile = Profile(
                    id = state.id,
                    name = state.name,
                    intervalMinutes = state.intervalMinutes,
                    totalSessionMinutes = state.totalSessionMinutes,
                    breakAfterIntervals = state.breakAfterIntervals,
                    breakDurationMinutes = state.breakDurationMinutes,
                    vibrationEnabled = state.vibrationEnabled,
                    isDefault = state.isDefault,
                    iconName = state.iconName,
                    colorHex = state.colorHex
                )
                profileRepository.deleteProfile(profile)
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}
