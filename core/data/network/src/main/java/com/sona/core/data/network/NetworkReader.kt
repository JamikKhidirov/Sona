package com.sona.core.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NetworkAddress(
    val interfaceName: String,
    val addresses: List<String>,
)

data class NetworkInfo(
    val isConnected: Boolean,
    val transport: String,
    val isMetered: Boolean,
    val isValidated: Boolean,
    val isVpn: Boolean,
    val downstreamKbps: Int,
    val upstreamKbps: Int,
    val addresses: List<NetworkAddress>,
)

interface NetworkReader {
    suspend fun read(): NetworkInfo
}

class AndroidNetworkReader(private val context: Context) : NetworkReader {

    override suspend fun read(): NetworkInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val network = manager?.activeNetwork
        val capabilities = network?.let { manager.getNetworkCapabilities(it) }
        val linkProperties = network?.let { manager.getLinkProperties(it) }

        fun has(transport: Int) = capabilities?.hasTransport(transport) == true

        NetworkInfo(
            isConnected = capabilities != null,
            transport = when {
                capabilities == null -> "None"
                has(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                has(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                has(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                has(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
                has(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                else -> "Other"
            },
            isMetered = manager?.isActiveNetworkMetered ?: false,
            isValidated = capabilities?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED,
            ) == true,
            isVpn = has(NetworkCapabilities.TRANSPORT_VPN),
            downstreamKbps = capabilities?.linkDownstreamBandwidthKbps ?: 0,
            upstreamKbps = capabilities?.linkUpstreamBandwidthKbps ?: 0,
            addresses = linkProperties?.linkAddresses.orEmpty().map { linkAddress ->
                NetworkAddress(
                    interfaceName = linkProperties?.interfaceName.orEmpty(),
                    addresses = listOf(linkAddress.address.hostAddress.orEmpty()),
                )
            },
        )
    }
}
