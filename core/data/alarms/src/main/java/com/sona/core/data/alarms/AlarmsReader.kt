package com.sona.core.data.alarms

import android.app.AlarmManager
import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AlarmInfo(
    val triggerAtMillis: Long?,
    val label: String?,
    val showIntentAvailable: Boolean,
)

interface AlarmsReader {
    suspend fun read(): AlarmInfo
}

class AndroidAlarmsReader(private val context: Context) : AlarmsReader {

    override suspend fun read(): AlarmInfo = withContext(Dispatchers.IO) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val clock = runCatching { alarmManager?.nextAlarmClock }.getOrNull()
        val label = runCatching {
            Settings.System.getString(context.contentResolver, Settings.System.NEXT_ALARM_FORMATTED)
        }.getOrNull()?.takeIf { it.isNotBlank() }
        AlarmInfo(
            triggerAtMillis = clock?.triggerTime,
            label = label,
            showIntentAvailable = clock?.showIntent != null,
        )
    }
}
