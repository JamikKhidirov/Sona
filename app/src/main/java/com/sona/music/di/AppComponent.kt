package com.sona.music.di

import android.content.Context
import com.sona.core.audio.impl.di.AudioModule
import com.sona.core.audio.impl.di.PlayerModule
import com.sona.core.audio.impl.service.PlaybackService
import com.sona.core.common.di.ApplicationContext
import com.sona.core.common.di.CoreDataModule
import com.sona.core.common.presentation.SonaViewModelFactory
import com.sona.core.network.audius.api.PlaylistApi
import com.sona.core.network.audius.api.SearchApi
import com.sona.core.network.audius.api.TrackApi
import com.sona.core.network.audius.api.UserApi
import com.sona.core.network.audius.di.ApiModule
import com.sona.core.network.audius.di.MusicModule
import com.sona.core.network.audius.di.NetworkModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        NetworkModule::class,
        ApiModule::class,
        MusicModule::class,
        CoreDataModule::class,
        AudioModule::class,
        PlayerModule::class,
        ViewModelBindingModule::class,
    ],
)
interface AppComponent {

    fun inject(service: PlaybackService)

    fun viewModelFactory(): SonaViewModelFactory

    /** Domain APIs that can be handed out to any feature or debug tooling. */
    fun trackApi(): TrackApi

    fun playlistApi(): PlaylistApi

    fun userApi(): UserApi

    fun searchApi(): SearchApi

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance @ApplicationContext context: Context): AppComponent
    }
}
