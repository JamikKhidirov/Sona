package com.sona.core.common.di

import com.sona.core.common.auth.TokenProvider
import com.sona.core.common.data.repository.LibraryRepositoryImpl
import com.sona.core.common.data.repository.SettingsRepositoryImpl
import com.sona.core.common.domain.repository.LibraryRepository
import com.sona.core.common.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class CoreDataModule {

    @Binds
    @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: SettingsRepositoryImpl): TokenProvider
}
