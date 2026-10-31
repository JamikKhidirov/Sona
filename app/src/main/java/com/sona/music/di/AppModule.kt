package com.sona.music.di

import com.sona.core.common.coroutines.DefaultDispatcherProvider
import com.sona.core.common.coroutines.DispatcherProvider
import com.sona.core.common.di.ApplicationScope
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    companion object {
        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(dispatchers: DispatcherProvider): CoroutineScope =
            CoroutineScope(SupervisorJob() + dispatchers.default)
    }
}
