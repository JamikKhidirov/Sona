package com.sona.core.data.hardware

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HardwareInfo(
    val cpuCores: Int,
    val supportedAbis: List<String>,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val lowMemory: Boolean,
    val hardwareName: String,
    val socManufacturer: String?,
    val socModel: String?,
)

interface HardwareReader {
    suspend fun read(): HardwareInfo
}

class AndroidHardwareReader(private val context: Context) : HardwareReader {

    override suspend fun read(): HardwareInfo = withContext(Dispatchers.IO) {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo()
        runCatching { activityManager?.getMemoryInfo(memoryInfo) }

        HardwareInfo(
            cpuCores = Runtime.getRuntime().availableProcessors(),
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            totalRamBytes = memoryInfo.totalMem,
            availableRamBytes = memoryInfo.availMem,
            lowMemory = memoryInfo.lowMemory,
            hardwareName = Build.HARDWARE,
            socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MANUFACTURER
            } else {
                null
            },
            socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MODEL
            } else {
                null
            },
        )
    }
}
