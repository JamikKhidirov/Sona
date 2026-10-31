package com.sona.core.audio.impl.service

import com.sona.core.audio.impl.service.PlaybackService

/** Implemented by the application so the media service stays independent of the app module. */
interface PlaybackServiceInjector {
    fun inject(service: PlaybackService)
}
