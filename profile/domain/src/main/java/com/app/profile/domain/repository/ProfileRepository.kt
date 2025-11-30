package com.app.profile.domain.repository

import com.app.profile.domain.entities.UserProfile

interface ProfileRepository {
    suspend fun getCurrentUserProfile(): UserProfile
}
