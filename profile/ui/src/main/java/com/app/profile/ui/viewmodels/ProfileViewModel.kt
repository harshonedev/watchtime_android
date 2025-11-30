package com.app.profile.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.auth.domain.repository.AuthRepository
import com.app.profile.domain.repository.ProfileRepository
import com.app.profile.ui.states.ProfileState
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = mutableStateOf<ProfileState>(ProfileState.Loading)
    val profileState = _profileState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val userProfile = profileRepository.getCurrentUserProfile()
                _profileState.value = ProfileState.Success(userProfile)
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _profileState.value = ProfileState.Loading
                val success = authRepository.logout()
                if (success) {
                    _profileState.value = ProfileState.LoggedOut
                } else {
                    _profileState.value = ProfileState.Error("Failed to logout")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Logout failed")
            }
        }
    }

    fun refreshProfile() {
        loadProfile()
    }
}
