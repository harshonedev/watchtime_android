package com.app.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val success: Boolean,
    val data: UserProfileData
)

@Serializable
data class UserSetupResponse(
    val success: Boolean,
    val data: UserSetupData
)

@Serializable
data class UserSetupData(
    val profile: UserProfile,
    val collections: List<UserCollection>
)

@Serializable
data class UserProfileData(
    val id: String,
    val email: String,
    val profile: UserProfile
)

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    val preferences: Map<String, String> = emptyMap(), // Defaults to empty map since JSON shows {}
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

@Serializable
data class UserCollection(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val name: String,
    val description: String,
    @SerialName("is_default")
    val isDefault: Boolean,
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
