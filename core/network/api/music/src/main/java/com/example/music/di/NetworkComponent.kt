package com.example.music.di

import android.app.Activity
import android.app.Service
import com.example.music.di.modules.NeworkModule
import com.example.music.di.modules.ServiceModule
import dagger.Subcomponent
import javax.inject.Singleton

@Singleton
@Subcomponent(modules = [
    NeworkModule::class,
    ServiceModule::class
])
interface NetworkComponent {

    fun inject(activity: Activity)

    fun inject(servive: Service)



    //Тут фабрика этого субкомпонента
    @Subcomponent.Factory
    interface Factory{
        fun create(): NetworkComponent
    }
}