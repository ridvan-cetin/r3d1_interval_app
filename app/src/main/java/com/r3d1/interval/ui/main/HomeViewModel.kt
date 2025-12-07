package com.r3d1.interval.ui.main

import androidx.lifecycle.ViewModel
import com.r3d1.interval.data.model.Profile
import com.r3d1.interval.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val profiles: Flow<List<Profile>> = profileRepository.getAllProfiles()
}
