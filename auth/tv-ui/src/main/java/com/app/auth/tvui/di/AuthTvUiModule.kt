package com.app.auth.tvui.di

import com.app.auth.tvui.viewmodels.TvAuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authTvUiModule = module {
    viewModel { TvAuthViewModel(get(), get()) }
}

