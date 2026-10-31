package com.sona.core.data.di

import com.sona.core.data.DeviceInfoCollector
import com.sona.core.data.DeviceSnapshotProvider
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface DeviceDataModule {

    @Binds
    @Singleton
    fun bindDeviceSnapshotProvider(collector: DeviceInfoCollector): DeviceSnapshotProvider
}
