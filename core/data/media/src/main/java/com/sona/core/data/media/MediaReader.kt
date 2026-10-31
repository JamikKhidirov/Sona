package com.sona.core.data.media

import android.Manifest
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.sona.core.common.android.isPermissionGranted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MediaTrack(
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val isMusic: Boolean,
)

data class MediaInfo(
    val permissionGranted: Boolean,
    val audioCount: Int,
    val albumCount: Int,
    val artistCount: Int,
    val totalDurationMs: Long,
    val tracks: List<MediaTrack>,
)

interface MediaReader {
    suspend fun read(limit: Int = 100): MediaInfo
}

class AndroidMediaReader(private val context: Context) : MediaReader {

    override suspend fun read(limit: Int): MediaInfo = withContext(Dispatchers.IO) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (!context.isPermissionGranted(permission)) {
            return@withContext MediaInfo(
                permissionGranted = false,
                audioCount = 0,
                albumCount = 0,
                artistCount = 0,
                totalDurationMs = 0L,
                tracks = emptyList(),
            )
        }

        val tracks = mutableListOf<MediaTrack>()
        val albums = mutableSetOf<String>()
        val artists = mutableSetOf<String>()
        var count = 0
        var totalDuration = 0L

        runCatching {
            val projection = arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.IS_MUSIC,
            )
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null,
            )?.use { cursor ->
                val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val musicIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
                while (cursor.moveToNext()) {
                    count++
                    val album = cursor.getString(albumIndex)
                    val artist = cursor.getString(artistIndex)
                    val duration = cursor.getLong(durationIndex)
                    totalDuration += duration
                    album?.let(albums::add)
                    artist?.let(artists::add)
                    if (tracks.size < limit) {
                        tracks.add(
                            MediaTrack(
                                title = cursor.getString(titleIndex),
                                artist = artist,
                                album = album,
                                durationMs = duration,
                                isMusic = cursor.getInt(musicIndex) != 0,
                            ),
                        )
                    }
                }
            }
        }

        MediaInfo(
            permissionGranted = true,
            audioCount = count,
            albumCount = albums.size,
            artistCount = artists.size,
            totalDurationMs = totalDuration,
            tracks = tracks,
        )
    }
}
