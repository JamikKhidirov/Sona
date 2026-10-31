package com.sona.music

import android.app.Application
import com.sona.core.audio.impl.service.PlaybackService
import com.sona.core.audio.impl.service.PlaybackServiceInjector
import com.sona.music.di.AppComponent
import com.sona.music.di.DaggerAppComponent

class SonaApplication : Application(), PlaybackServiceInjector {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
    }

    override fun inject(service: PlaybackService) {
        appComponent.inject(service)
    }
}
