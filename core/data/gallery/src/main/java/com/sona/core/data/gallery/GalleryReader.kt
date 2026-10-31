package com.sona.core.data.gallery

import android.Manifest
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.sona.core.common.android.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GalleryItem(
    val name: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val dateAddedSeconds: Long,
)

data class GalleryInfo(
    val permissionGranted: Boolean,
    val imageCount: Int,
    val videoCount: Int,
    val recent: List<GalleryItem>,
)

interface GalleryReader {
    suspend fun read(limit: Int = 60): GalleryInfo
}

class AndroidGalleryReader(private val context: Context) : GalleryReader {

    override suspend fun read(limit: Int): GalleryInfo = withContext(Dispatchers.IO) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (!context.isPermissionGranted(permission)) {
            return@withContext GalleryInfo(
                permissionGranted = false,
                imageCount = 0,
                videoCount = 0,
                recent = emptyList(),
            )
        }

        val recent = mutableListOf<GalleryItem>()
        var images = 0
        var videos = 0

        runCatching {
            val projection = arrayOf(
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_ADDED,
            )
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_ADDED} DESC",
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                )
                val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                while (cursor.moveToNext()) {
                    val mime = cursor.getString(mimeIndex)
                    if (mime == null) continue
                    when {
                        mime.startsWith("image/") -> images++
                        mime.startsWith("video/") -> videos++
                        else -> continue
                    }
                    if (recent.size < limit) {
                        recent.add(
                            GalleryItem(
                                name = cursor.getString(nameIndex).orEmpty(),
                                mimeType = mime,
                                sizeBytes = cursor.getLong(sizeIndex),
                                dateAddedSeconds = cursor.getLong(dateIndex),
                            ),
                        )
                    }
                }
            }
        }

        GalleryInfo(
            permissionGranted = true,
            imageCount = images,
            videoCount = videos,
            recent = recent,
        )
    }
}
