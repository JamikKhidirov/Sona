package com.sona.core.data.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import com.sona.core.common.android.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PairedDevice(
    val name: String?,
    val address: String?,
    val isConnected: Boolean,
)

data class BluetoothInfo(
    val supported: Boolean,
    val enabled: Boolean,
    val adapterName: String?,
    val pairedDevices: List<PairedDevice>,
    val permissionGranted: Boolean,
)

interface BluetoothReader {
    suspend fun read(): BluetoothInfo
}

class AndroidBluetoothReader(private val context: Context) : BluetoothReader {

    override suspend fun read(): BluetoothInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(BluetoothManager::class.java)
        val adapter: BluetoothAdapter? = manager?.adapter
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            true
        }

        val paired = if (adapter != null && permissionGranted) {
            val connectedAddresses = runCatching {
                manager.getConnectedDevices(BluetoothProfile.GATT)
                    .mapNotNull { it.address }
                    .toSet()
            }.getOrDefault(emptySet())
            runCatching {
                adapter.bondedDevices.orEmpty().map { device ->
                    PairedDevice(
                        name = device.name,
                        address = device.address,
                        isConnected = device.address in connectedAddresses,
                    )
                }
            }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        BluetoothInfo(
            supported = adapter != null,
            enabled = runCatching { adapter?.isEnabled == true }.getOrDefault(false),
            adapterName = runCatching { adapter?.name }.getOrNull(),
            pairedDevices = paired,
            permissionGranted = permissionGranted,
        )
    }
}
