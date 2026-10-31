package com.sona.core.data.device

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DeviceInfo(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val product: String,
    val hardware: String,
    val board: String,
    val buildId: String,
    val fingerprint: String,
    val androidVersion: String,
    val sdkInt: Int,
    val isEmulator: Boolean,
    val locale: String,
    val timeZone: String,
    val uptimeMinutes: Long,
    val deviceName: String?,
)

interface DeviceReader {
    suspend fun read(): DeviceInfo
}

class AndroidDeviceReader(private val context: Context) : DeviceReader {

    override suspend fun read(): DeviceInfo = withContext(Dispatchers.IO) {
        DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            brand = Build.BRAND,
            model = Build.MODEL,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            hardware = Build.HARDWARE,
            board = Build.BOARD,
            buildId = Build.ID,
            fingerprint = Build.FINGERPRINT,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.lowercase().contains("emulator") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu"),
            locale = Locale.getDefault().toLanguageTag(),
            timeZone = java.util.TimeZone.getDefault().id,
            uptimeMinutes = TimeUnit.MILLISECONDS.toMinutes(SystemClock.elapsedRealtime()),
            deviceName = runCatching {
                Settings.Global.getString(context.contentResolver, "device_name")
            }.getOrNull(),
        )
    }
}
