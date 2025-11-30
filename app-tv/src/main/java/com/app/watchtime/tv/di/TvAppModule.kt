package com.app.watchtime.tv.di

import com.app.watchtime.tv.viewmodel.TvHomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val tvAppModule = module {
    // ViewModels
    viewModel { TvHomeViewModel(get(), get(), get()) }
}

