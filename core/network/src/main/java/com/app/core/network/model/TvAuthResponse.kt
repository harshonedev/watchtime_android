package com.app.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TvAuthSessionResponse(
    val success: Boolean,
    val data: TvAuthSessionData
)

@Serializable
data class TvAuthSessionData(
    val sessionId: String,
    val authUrl: String,
    val expiresAt: Long
)

@Serializable
data class TvAuthStatusResponse(
    val success: Boolean,
    val data: TvAuthStatusData
)

@Serializable
data class TvAuthStatusData(
    val authenticated: Boolean,
    val token: String? = null,
    val userId: String? = null
)

@Serializable
data class TvAuthLinkResponse(
    val success: Boolean,
    val message: String
)

