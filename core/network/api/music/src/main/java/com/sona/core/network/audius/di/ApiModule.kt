package com.sona.core.network.audius.di

import com.sona.core.network.audius.api.PlaylistApi
import com.sona.core.network.audius.api.SearchApi
import com.sona.core.network.audius.api.TrackApi
import com.sona.core.network.audius.api.UserApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Exposes every Audius domain API through Dagger so features can inject only the
 * interface they need (tracks, playlists, accounts, search).
 */
@Module
object ApiModule {

    @Provides
    @Singleton
    fun provideTrackApi(retrofit: Retrofit): TrackApi =
        retrofit.create(TrackApi::class.java)

    @Provides
    @Singleton
    fun providePlaylistApi(retrofit: Retrofit): PlaylistApi =
        retrofit.create(PlaylistApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideSearchApi(retrofit: Retrofit): SearchApi =
        retrofit.create(SearchApi::class.java)
}
