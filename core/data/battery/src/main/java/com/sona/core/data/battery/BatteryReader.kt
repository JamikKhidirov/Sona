package com.sona.core.data.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BatteryInfo(
    val levelPercent: Int,
    val isCharging: Boolean,
    val isPlugged: Boolean,
    val plugType: String,
    val health: String,
    val temperatureCelsius: Float,
    val voltageMillivolts: Int,
    val technology: String?,
    val chargeCounterMicroAmpHours: Int?,
    val statusLabel: String,
)

interface BatteryReader {
    suspend fun read(): BatteryInfo
}

class AndroidBatteryReader(private val context: Context) : BatteryReader {

    override suspend fun read(): BatteryInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(BatteryManager::class.java)
        val intent: Intent? = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
        )

        fun intExtra(key: String, default: Int = -1): Int =
            intent?.getIntExtra(key, default) ?: default

        val level = intExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intExtra(BatteryManager.EXTRA_SCALE, 100).takeIf { it > 0 } ?: 100
        val status = intExtra(BatteryManager.EXTRA_STATUS)
        val plugged = intExtra(BatteryManager.EXTRA_PLUGGED, 0)

        BatteryInfo(
            levelPercent = (level * 100 / scale).coerceIn(0, 100),
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL,
            isPlugged = plugged != 0,
            plugType = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Unplugged"
            },
            health = when (intExtra(BatteryManager.EXTRA_HEALTH)) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            },
            temperatureCelsius = intExtra(BatteryManager.EXTRA_TEMPERATURE) / 10f,
            voltageMillivolts = intExtra(BatteryManager.EXTRA_VOLTAGE),
            technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY),
            chargeCounterMicroAmpHours = runCatching {
                manager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            }.getOrNull(),
            statusLabel = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
                else -> "Unknown"
            },
        )
    }
}
