package com.r3d1.interval.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r3d1.interval.data.model.Profile
import com.r3d1.interval.data.model.TimerState
import com.r3d1.interval.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    fun loadProfile(profileId: Long) {
        viewModelScope.launch {
            _profile.value = profileRepository.getProfileById(profileId)
        }
    }

    fun updateTimerState(state: TimerState) {
        _timerState.value = state
    }
}
