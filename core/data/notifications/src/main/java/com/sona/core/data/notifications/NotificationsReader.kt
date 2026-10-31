package com.sona.core.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NotificationChannelInfo(
    val id: String,
    val name: String?,
    val importance: Int,
    val isBlocked: Boolean,
)

data class NotificationsInfo(
    val enabled: Boolean,
    val policyAccessGranted: Boolean,
    val channels: List<NotificationChannelInfo>,
    val activeNotificationCount: Int,
)

interface NotificationsReader {
    suspend fun read(): NotificationsInfo
}

class AndroidNotificationsReader(private val context: Context) : NotificationsReader {

    override suspend fun read(): NotificationsInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                manager?.notificationChannels.orEmpty().map { it.toInfo() }
            }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        NotificationsInfo(
            enabled = manager?.areNotificationsEnabled() ?: false,
            policyAccessGranted = manager?.isNotificationPolicyAccessGranted ?: false,
            channels = channels,
            activeNotificationCount = runCatching {
                manager?.activeNotifications?.size ?: 0
            }.getOrDefault(0),
        )
    }

    private fun NotificationChannel.toInfo() = NotificationChannelInfo(
        id = id,
        name = name?.toString(),
        importance = importance,
        isBlocked = importance == NotificationManager.IMPORTANCE_NONE,
    )
}
