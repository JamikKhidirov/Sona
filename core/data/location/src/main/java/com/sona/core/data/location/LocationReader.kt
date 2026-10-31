package com.sona.core.data.location

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.sona.core.common.android.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LocationFix(
    val provider: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val altitudeMeters: Double?,
    val timeMillis: Long,
    val isMock: Boolean,
)

data class LocationInfo(
    val permissionGranted: Boolean,
    val providersEnabled: List<String>,
    val lastKnown: List<LocationFix>,
)

interface LocationReader {
    suspend fun read(): LocationInfo
}

class AndroidLocationReader(private val context: Context) : LocationReader {

    override suspend fun read(): LocationInfo = withContext(Dispatchers.IO) {
        val granted = context.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            context.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        val manager = context.getSystemService(LocationManager::class.java)

        val enabled = runCatching {
            manager?.getProviders(true).orEmpty()
        }.getOrDefault(emptyList())

        val fixes = if (granted && manager != null) {
            runCatching {
                manager.getProviders(true).mapNotNull { provider ->
                    manager.getLastKnownLocation(provider)?.toFix(provider)
                }
            }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        LocationInfo(
            permissionGranted = granted,
            providersEnabled = enabled,
            lastKnown = fixes,
        )
    }

    private fun Location.toFix(provider: String) = LocationFix(
        provider = provider,
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracy,
        altitudeMeters = if (hasAltitude()) altitude else null,
        timeMillis = time,
        isMock = isFromMockProvider,
    )
}
