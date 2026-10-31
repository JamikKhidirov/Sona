package com.sona.core.data.usb

import android.content.Context
import android.hardware.usb.UsbManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UsbDeviceInfo(
    val deviceName: String?,
    val vendorId: Int,
    val productId: Int,
    val manufacturerName: String?,
    val productName: String?,
    val interfaceCount: Int,
    val hasPermission: Boolean,
)

data class UsbInfo(
    val hasHostSupport: Boolean,
    val deviceCount: Int,
    val devices: List<UsbDeviceInfo>,
)

interface UsbReader {
    suspend fun read(): UsbInfo
}

class AndroidUsbReader(private val context: Context) : UsbReader {

    override suspend fun read(): UsbInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(UsbManager::class.java)
        val devices = runCatching {
            manager?.deviceList.orEmpty().values.map { device ->
                UsbDeviceInfo(
                    deviceName = device.deviceName,
                    vendorId = device.vendorId,
                    productId = device.productId,
                    manufacturerName = runCatching { device.manufacturerName }.getOrNull(),
                    productName = runCatching { device.productName }.getOrNull(),
                    interfaceCount = device.interfaceCount,
                    hasPermission = manager?.hasPermission(device) == true,
                )
            }
        }.getOrDefault(emptyList())

        UsbInfo(
            hasHostSupport = manager != null,
            deviceCount = devices.size,
            devices = devices,
        )
    }
}
