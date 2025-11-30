package com.app.auth.ui.di

import com.app.auth.domain.repository.AuthRepository
import com.app.auth.ui.viewmodels.AuthViewModel
import com.app.auth.ui.viewmodels.QrScanViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authUiModule = module {
    viewModel<AuthViewModel> {
        AuthViewModel(
            get<AuthRepository>()
        )
    }

    viewModel<QrScanViewModel> {
        QrScanViewModel(
            get(),
            get<AuthRepository>()
        )
    }
}