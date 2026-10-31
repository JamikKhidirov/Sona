package com.sona.core.network.audius.di

import com.sona.core.common.domain.repository.MusicRepository
import com.sona.core.network.audius.repository.MusicRepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class MusicModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
}
