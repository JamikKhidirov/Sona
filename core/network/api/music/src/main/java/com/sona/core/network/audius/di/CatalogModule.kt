package com.sona.core.network.audius.di

import com.sona.core.common.domain.repository.ArtistRepository
import com.sona.core.common.domain.repository.CommentRepository
import com.sona.core.common.domain.repository.DiscoveryRepository
import com.sona.core.network.audius.repository.ArtistRepositoryImpl
import com.sona.core.network.audius.repository.CommentRepositoryImpl
import com.sona.core.network.audius.repository.DiscoveryRepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/**
 * Binds the extra Audius read surfaces introduced with the artist, comments
 * and explore screens. They are intentionally split by responsibility.
 */
@Module
abstract class CatalogModule {

    @Binds
    @Singleton
    abstract fun bindArtistRepository(impl: ArtistRepositoryImpl): ArtistRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindDiscoveryRepository(impl: DiscoveryRepositoryImpl): DiscoveryRepository
}
