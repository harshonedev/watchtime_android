package com.app.core.network.di

import com.app.core.network.api.CollectionsApiService
import com.app.core.network.api.TMDBApiService
import com.app.core.network.api.TvAuthApiService
import com.app.core.network.api.UserApiService
import com.app.core.utils.constants.Constants
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val networkModule = module {

    // JSON configuration
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }


    // OkHttp Client
    single {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit instance
    single {
        Retrofit.Builder()
            .baseUrl(Constants.API_SERVER_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // TMDB API Services
    single<TMDBApiService> {
        Retrofit.Builder()
            .baseUrl(Constants.TMDB_BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TMDBApiService::class.java)
    }

    // User Profile API Service
    single<UserApiService> {
        get<Retrofit>().create(UserApiService::class.java)
    }

    // Collections API Service
    single<CollectionsApiService> {
        get<Retrofit>().create(CollectionsApiService::class.java)
    }

    // TV Auth API Service
    single<TvAuthApiService> {
        get<Retrofit>().create(TvAuthApiService::class.java)
    }
}
