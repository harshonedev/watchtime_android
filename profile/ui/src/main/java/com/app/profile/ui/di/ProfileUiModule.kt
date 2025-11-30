package com.app.profile.ui.di

import com.app.profile.ui.viewmodels.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileUiModule = module {
    viewModel<ProfileViewModel> {
        ProfileViewModel(
            profileRepository = get(),
            authRepository = get()
        )
    }
}
