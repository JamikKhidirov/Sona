package com.sona.core.data.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BrowserApp(
    val packageName: String,
    val label: String,
    val isDefault: Boolean,
)

data class BrowserInfo(
    val defaultBrowserPackage: String?,
    val installedBrowsers: List<BrowserApp>,
)

interface BrowserReader {
    suspend fun read(): BrowserInfo
}

class AndroidBrowserReader(private val context: Context) : BrowserReader {

    override suspend fun read(): BrowserInfo = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://audius.co")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        val defaultPackage = runCatching {
            pm.resolveActivity(probe, 0)?.activityInfo?.packageName
        }.getOrNull()

        val browsers = runCatching {
            pm.queryIntentActivities(probe, 0)
                .map { it.activityInfo.packageName }
                .distinct()
                .map { pkg ->
                    BrowserApp(
                        packageName = pkg,
                        label = runCatching {
                            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                        }.getOrDefault(pkg),
                        isDefault = pkg == defaultPackage,
                    )
                }
                .sortedBy { it.label.lowercase() }
        }.getOrDefault(emptyList())

        BrowserInfo(defaultBrowserPackage = defaultPackage, installedBrowsers = browsers)
    }
}
