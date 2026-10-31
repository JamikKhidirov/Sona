package com.sona.core.data.downloads

import android.app.DownloadManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DownloadEntry(
    val id: Long,
    val title: String?,
    val status: String,
    val sizeBytes: Long,
    val downloadedBytes: Long,
    val localUri: String?,
)

data class DownloadsInfo(
    val count: Int,
    val downloads: List<DownloadEntry>,
)

interface DownloadsReader {
    suspend fun read(limit: Int = 100): DownloadsInfo
}

class AndroidDownloadsReader(private val context: Context) : DownloadsReader {

    override suspend fun read(limit: Int): DownloadsInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(DownloadManager::class.java)
        val entries = mutableListOf<DownloadEntry>()

        runCatching {
            manager?.query(DownloadManager.Query().setFilterByStatus(
                DownloadManager.STATUS_SUCCESSFUL,
            ))?.use { cursor ->
                val idIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                val titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val sizeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val bytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                while (cursor.moveToNext() && entries.size < limit) {
                    entries.add(
                        DownloadEntry(
                            id = cursor.getLong(idIndex),
                            title = cursor.getString(titleIndex),
                            status = when (cursor.getInt(statusIndex)) {
                                DownloadManager.STATUS_SUCCESSFUL -> "Completed"
                                DownloadManager.STATUS_RUNNING -> "Running"
                                DownloadManager.STATUS_PAUSED -> "Paused"
                                DownloadManager.STATUS_FAILED -> "Failed"
                                else -> "Pending"
                            },
                            sizeBytes = cursor.getLong(sizeIndex),
                            downloadedBytes = cursor.getLong(bytesIndex),
                            localUri = cursor.getString(uriIndex),
                        ),
                    )
                }
            }
        }

        DownloadsInfo(count = entries.size, downloads = entries)
    }
}
