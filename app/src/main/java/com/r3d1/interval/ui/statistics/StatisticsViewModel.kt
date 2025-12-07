package com.r3d1.interval.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r3d1.interval.data.model.DailyStats
import com.r3d1.interval.data.model.ProfileStats
import com.r3d1.interval.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val totalMinutes: Flow<Int?> = sessionRepository.getTotalMinutes(7)
    val completedSessions: Flow<Int> = sessionRepository.getCompletedSessionsCount(7)
    val activeDays: Flow<Int> = sessionRepository.getActiveDaysCount(7)
    val dailyStats: Flow<List<DailyStats>> = sessionRepository.getDailyStats(7)
    val profileStats: Flow<List<ProfileStats>> = sessionRepository.getProfileStats(30)

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    init {
        loadStreak()
    }

    private fun loadStreak() {
        viewModelScope.launch {
            _currentStreak.value = sessionRepository.calculateCurrentStreak()
        }
    }
}
