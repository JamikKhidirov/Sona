package com.sona.core.data.vpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VpnNetwork(
    val interfaceName: String?,
    val addresses: List<String>,
    val dnsServers: List<String>,
)

data class VpnInfo(
    val active: Boolean,
    val networks: List<VpnNetwork>,
)

interface VpnReader {
    suspend fun read(): VpnInfo
}

class AndroidVpnReader(private val context: Context) : VpnReader {

    override suspend fun read(): VpnInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(ConnectivityManager::class.java)
        val networks = runCatching {
            manager?.allNetworks.orEmpty().mapNotNull { network ->
                val capabilities = manager?.getNetworkCapabilities(network) ?: return@mapNotNull null
                if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    return@mapNotNull null
                }
                val link = manager.getLinkProperties(network)
                VpnNetwork(
                    interfaceName = link?.interfaceName,
                    addresses = link?.linkAddresses.orEmpty()
                        .map { it.address.hostAddress.orEmpty() },
                    dnsServers = link?.dnsServers.orEmpty()
                        .map { it.hostAddress.orEmpty() },
                )
            }
        }.getOrDefault(emptyList())

        VpnInfo(active = networks.isNotEmpty(), networks = networks)
    }
}
