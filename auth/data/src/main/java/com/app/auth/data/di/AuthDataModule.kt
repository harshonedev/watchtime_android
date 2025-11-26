package com.app.auth.data.di

import android.content.Context
import com.app.auth.data.R
import com.app.auth.data.repository.AuthRepositoryImpl
import com.app.auth.data.services.FirebaseAuthService
import com.app.auth.data.services.SupabaseAuthService
import com.app.auth.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.ktor.http.parameters
import io.ktor.http.parametersOf
import org.koin.dsl.module

val authDataModule = module {

    single<FirebaseAuthService> {
        FirebaseAuthService(
            get<Context>(),
        )
    }

    single<SupabaseAuthService>  {
        SupabaseAuthService(
            get<SupabaseClient>()
        )
    }

    single<AuthRepository> {
        AuthRepositoryImpl(get<FirebaseAuthService>(), get<SupabaseAuthService>())
    }

    single<SupabaseClient> { createSupabaseClient(
        supabaseKey = get<Context>().getString(R.string.supabase_anon_key),
        supabaseUrl = get<Context>().getString(R.string.supabase_url)
    ) {
        install(Auth)
    } }

}

