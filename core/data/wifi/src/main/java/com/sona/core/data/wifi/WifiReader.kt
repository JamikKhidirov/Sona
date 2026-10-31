package com.sona.core.data.wifi

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WifiInfoSnapshot(
    val enabled: Boolean,
    val ssid: String?,
    val bssid: String?,
    val linkSpeedMbps: Int,
    val frequencyMhz: Int,
    val rssiDbm: Int,
    val ipAddress: String?,
    val isHiddenSsid: Boolean,
)

interface WifiReader {
    suspend fun read(): WifiInfoSnapshot
}

class AndroidWifiReader(private val context: Context) : WifiReader {

    override suspend fun read(): WifiInfoSnapshot = withContext(Dispatchers.IO) {
        val manager = context.applicationContext.getSystemService(WifiManager::class.java)
        val info = runCatching { manager?.connectionInfo }.getOrNull()

        WifiInfoSnapshot(
            enabled = runCatching { manager?.isWifiEnabled == true }.getOrDefault(false),
            ssid = runCatching { info?.ssid }.getOrNull()
                ?.takeIf { it != "<unknown ssid>" },
            bssid = runCatching { info?.bssid }.getOrNull(),
            linkSpeedMbps = runCatching { info?.linkSpeed ?: 0 }.getOrDefault(0),
            frequencyMhz = runCatching { info?.frequency ?: 0 }.getOrDefault(0),
            rssiDbm = runCatching { info?.rssi ?: 0 }.getOrDefault(0),
            ipAddress = runCatching { info?.ipAddress ?: 0 }.getOrNull()
                ?.takeIf { it != 0 }
                ?.let { ip ->
                    listOf(
                        ip and 0xff,
                        ip shr 8 and 0xff,
                        ip shr 16 and 0xff,
                        ip shr 24 and 0xff,
                    ).joinToString(".")
                },
            isHiddenSsid = runCatching { info?.hiddenSSID == true }.getOrDefault(false),
        )
    }
}
