package com.sona.core.data.documents

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.sona.core.common.android.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DocumentEntry(
    val name: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val modifiedAtMillis: Long,
    val uri: String,
)

data class DocumentsInfo(
    val permissionGranted: Boolean,
    val count: Int,
    val documents: List<DocumentEntry>,
)

interface DocumentsReader {
    suspend fun read(limit: Int = 200): DocumentsInfo
}

class AndroidDocumentsReader(private val context: Context) : DocumentsReader {

    override suspend fun read(limit: Int): DocumentsInfo = withContext(Dispatchers.IO) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Media permissions do not cover arbitrary documents; access is best effort.
            null
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = permission == null || context.isPermissionGranted(permission)

        val documents = mutableListOf<DocumentEntry>()
        runCatching {
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Files.getContentUri("external")
            }
            val projection = arrayOf(
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns._ID,
            )
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC",
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                )
                val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateIndex = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.DATE_MODIFIED,
                )
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                while (cursor.moveToNext() && documents.size < limit) {
                    val mime = cursor.getString(mimeIndex)
                    if (mime != null && !mime.startsWith("application/")) continue
                    documents.add(
                        DocumentEntry(
                            name = cursor.getString(nameIndex).orEmpty(),
                            mimeType = mime,
                            sizeBytes = cursor.getLong(sizeIndex),
                            modifiedAtMillis = cursor.getLong(dateIndex) * 1000L,
                            uri = "${collection}/${cursor.getLong(idIndex)}",
                        ),
                    )
                }
            }
        }

        DocumentsInfo(
            permissionGranted = granted,
            count = documents.size,
            documents = documents,
        )
    }
}
