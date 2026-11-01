package com.sona.core.audio.impl.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.sona.core.audio.AudioPlayer
import com.sona.core.audio.impl.player.ExoAudioPlayer
import com.sona.core.common.di.ApplicationContext
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class AudioModule {

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(impl: ExoAudioPlayer): AudioPlayer
}

