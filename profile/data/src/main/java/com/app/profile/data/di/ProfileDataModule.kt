package com.app.profile.data.di

import com.app.profile.data.repository.ProfileRepositoryImpl
import com.app.profile.domain.repository.ProfileRepository
import org.koin.dsl.module

val profileDataModule = module {
    single<ProfileRepository> {
        ProfileRepositoryImpl(
            supabaseAuthService = get(),
            userApiService = get()
        )
    }
}

