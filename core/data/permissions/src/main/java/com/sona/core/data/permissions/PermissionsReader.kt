package com.sona.core.data.permissions

import android.Manifest
import android.content.Context
import com.sona.core.common.android.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PermissionState(
    val permission: String,
    val label: String,
    val granted: Boolean,
)

data class PermissionsInfo(
    val grantedCount: Int,
    val totalCount: Int,
    val permissions: List<PermissionState>,
)

interface PermissionsReader {
    suspend fun read(): PermissionsInfo
}

class AndroidPermissionsReader(private val context: Context) : PermissionsReader {

    private val trackedPermissions: List<Pair<String, String>> = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION to "Precise location",
        Manifest.permission.ACCESS_COARSE_LOCATION to "Approximate location",
        Manifest.permission.CAMERA to "Camera",
        Manifest.permission.RECORD_AUDIO to "Microphone",
        Manifest.permission.READ_CONTACTS to "Contacts",
        Manifest.permission.READ_CALENDAR to "Calendar",
        Manifest.permission.READ_SMS to "SMS",
        Manifest.permission.READ_CALL_LOG to "Call log",
        Manifest.permission.CALL_PHONE to "Phone calls",
        Manifest.permission.READ_PHONE_STATE to "Phone state",
        Manifest.permission.BODY_SENSORS to "Body sensors",
        Manifest.permission.POST_NOTIFICATIONS to "Notifications",
        Manifest.permission.READ_MEDIA_IMAGES to "Photos",
        Manifest.permission.READ_MEDIA_VIDEO to "Videos",
        Manifest.permission.READ_MEDIA_AUDIO to "Music and audio",
        Manifest.permission.BLUETOOTH_CONNECT to "Bluetooth",
        Manifest.permission.NEARBY_WIFI_DEVICES to "Nearby Wi-Fi",
        Manifest.permission.ACTIVITY_RECOGNITION to "Activity recognition",
    )

    override suspend fun read(): PermissionsInfo = withContext(Dispatchers.IO) {
        val states = trackedPermissions.map { (permission, label) ->
            PermissionState(
                permission = permission,
                label = label,
                granted = runCatching { context.isPermissionGranted(permission) }
                    .getOrDefault(false),
            )
        }
        PermissionsInfo(
            grantedCount = states.count { it.granted },
            totalCount = states.size,
            permissions = states,
        )
    }
}
