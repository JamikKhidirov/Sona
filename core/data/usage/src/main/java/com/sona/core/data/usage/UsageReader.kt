package com.sona.core.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppUsage(
    val packageName: String,
    val foregroundMinutes: Long,
    val lastTimeUsedMillis: Long,
)

data class UsageInfo(
    val accessGranted: Boolean,
    val apps: List<AppUsage>,
)

interface UsageReader {
    suspend fun read(limit: Int = 50): UsageInfo
}

class AndroidUsageReader(private val context: Context) : UsageReader {

    override suspend fun read(limit: Int): UsageInfo = withContext(Dispatchers.IO) {
        if (!hasUsageAccess()) {
            return@withContext UsageInfo(accessGranted = false, apps = emptyList())
        }

        val manager = context.getSystemService(UsageStatsManager::class.java)
        val end = System.currentTimeMillis()
        val start = end - 24 * 60 * 60 * 1000L

        val apps = runCatching {
            manager?.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
                .orEmpty()
                .filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }
                .take(limit)
                .map { stats ->
                    AppUsage(
                        packageName = stats.packageName,
                        foregroundMinutes = stats.totalTimeInForeground / 60_000L,
                        lastTimeUsedMillis = stats.lastTimeUsed,
                    )
                }
        }.getOrDefault(emptyList())

        UsageInfo(accessGranted = true, apps = apps)
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
