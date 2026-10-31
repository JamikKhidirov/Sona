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

@Module
object PlayerModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
    }
}
