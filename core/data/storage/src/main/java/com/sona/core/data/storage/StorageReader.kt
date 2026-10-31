package com.sona.core.data.storage

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class StorageVolumeInfo(
    val name: String,
    val path: String?,
    val isRemovable: Boolean,
    val isPrimary: Boolean,
    val state: String,
)

data class StorageInfo(
    val internalTotalBytes: Long,
    val internalFreeBytes: Long,
    val externalTotalBytes: Long,
    val externalFreeBytes: Long,
    val volumes: List<StorageVolumeInfo>,
)

interface StorageReader {
    suspend fun read(): StorageInfo
}

class AndroidStorageReader(private val context: Context) : StorageReader {

    override suspend fun read(): StorageInfo = withContext(Dispatchers.IO) {
        val internal = Environment.getDataDirectory()
        val external = Environment.getExternalStorageDirectory()

        lateinit var volumes: List<StorageVolumeInfo>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val manager = context.getSystemService(StorageManager::class.java)
            volumes = runCatching {
                manager?.storageVolumes.orEmpty().map { it.toInfo() }
            }.getOrDefault(emptyList())
        } else {
            volumes = emptyList()
        }

        StorageInfo(
            internalTotalBytes = internal.totalSpaceSafe(),
            internalFreeBytes = internal.usableSpaceSafe(),
            externalTotalBytes = external.totalSpaceSafe(),
            externalFreeBytes = external.usableSpaceSafe(),
            volumes = volumes,
        )
    }

    private fun StorageVolume.toInfo(): StorageVolumeInfo {
        val directory: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            directory
        } else {
            null
        }
        return StorageVolumeInfo(
            name = getDescription(context),
            path = directory?.absolutePath,
            isRemovable = isRemovable,
            isPrimary = isPrimary,
            state = state,
        )
    }

    private fun File.totalSpaceSafe(): Long = runCatching { totalSpace }.getOrDefault(0L)
    private fun File.usableSpaceSafe(): Long = runCatching { usableSpace }.getOrDefault(0L)
}
