package com.app.collections.data.module

import com.app.auth.data.services.SupabaseAuthService
import com.app.collections.data.remote.CollectionRemoteDataSource
import com.app.collections.data.repository.CollectionRepositoryImpl
import com.app.collections.domain.repository.CollectionRepository
import com.app.core.network.api.CollectionsApiService
import com.app.core.room.dao.CollectionDao
import com.app.core.room.database.AppDatabase
import org.koin.dsl.module

val collectionDataModule = module {
    
    // Remote data source
    single<CollectionRemoteDataSource> {
        CollectionRemoteDataSource(
            apiService = get<CollectionsApiService>(),
            getAuthToken = { get<SupabaseAuthService>().getToken() }
        )
    }
    
    // Repository
    single<CollectionRepository> {
        CollectionRepositoryImpl(
            collectionDao = get<CollectionDao>(),
            remoteDataSource = get<CollectionRemoteDataSource>()
        )
    }
}