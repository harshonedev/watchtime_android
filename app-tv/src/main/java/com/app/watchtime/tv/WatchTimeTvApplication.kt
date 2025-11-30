package com.app.watchtime.tv

import android.app.Application
import com.app.auth.data.di.authDataModule
import com.app.auth.tvui.di.authTvUiModule
import com.app.collections.data.module.collectionDataModule
import com.app.core.network.di.networkModule
import com.app.core.room.di.roomModule
import com.app.core.ui.di.coreUiModule
import com.app.discover.data.di.discoverDataModule
import com.app.media.data.di.mediaDataModule
import com.app.popular.data.di.popularDataModule
import com.app.profile.data.di.profileDataModule
import com.app.watchtime.tv.di.tvAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WatchTimeTvApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI
        startKoin {
            androidContext(this@WatchTimeTvApplication)
            modules(
                listOf(
                    // Core modules
                    networkModule,
                    roomModule,
                    coreUiModule,
                    tvAppModule,

                    // Auth modules
                    authDataModule,
                    authTvUiModule,

                    // Feature data modules
                    popularDataModule,
                    discoverDataModule,
                    mediaDataModule,
                    collectionDataModule,
                    profileDataModule
                )
            )
        }
    }
}

