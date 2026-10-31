package com.sona.core.data.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val isSystem: Boolean,
)

data class AppsInfo(
    val totalCount: Int,
    val systemCount: Int,
    val userApps: List<InstalledApp>,
)

interface AppsReader {
    suspend fun read(): AppsInfo
}

class AndroidAppsReader(private val context: Context) : AppsReader {

    @Suppress("DEPRECATION")
    override suspend fun read(): AppsInfo = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                pm.getInstalledPackages(0)
            }
        }.getOrDefault(emptyList())

        val userApps = packages
            .filter { it.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) == 0 }
            .mapNotNull { info ->
                runCatching {
                    InstalledApp(
                        packageName = info.packageName,
                        label = info.applicationInfo?.let { pm.getApplicationLabel(it).toString() }
                            ?: info.packageName,
                        versionName = info.versionName,
                        isSystem = false,
                    )
                }.getOrNull()
            }
            .sortedBy { it.label.lowercase() }

        AppsInfo(
            totalCount = packages.size,
            systemCount = packages.count {
                it.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0
            },
            userApps = userApps,
        )
    }
}
