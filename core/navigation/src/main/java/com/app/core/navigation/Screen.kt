package com.app.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    object Home : Screen()

    @Serializable
    object Auth : Screen()

    @Serializable
    object QrScan : Screen()

    @Serializable
    data class MediaDetails(
        val id: Int,
        val type: String,
        val posterUrl: String?,
        val posterKey: String
    ) : Screen()

    @Serializable
    data class Season(
        val tvId: Int,
        val seasonNumber: Int,
        val tvName: String,
        val seasonName: String,
        val posterPath: String?
    )
}