package com.sona.core.network.audius.mapper

import com.sona.core.common.model.Artist
import com.sona.core.common.model.Artwork
import com.sona.core.common.model.Playlist
import com.sona.core.common.model.Track
import com.sona.core.network.audius.dto.ArtworkDto
import com.sona.core.network.audius.dto.PlaylistDto
import com.sona.core.network.audius.dto.TrackDto
import com.sona.core.network.audius.dto.UserDto

private val UnknownArtist = Artist(
    id = "unknown",
    handle = "audius",
    name = "Audius",
)

fun ArtworkDto?.toDomain(): Artwork? = this?.let {
    Artwork(small = small, medium = medium, large = large)
}

fun UserDto.toDomain(): Artist = Artist(
    id = id.orEmpty(),
    handle = handle.orEmpty(),
    name = name.orEmpty(),
    avatarUrl = profilePicture.toDomain()?.bestUrl(),
    coverUrl = coverPhoto.toDomain()?.bestUrl(),
    bio = bio,
    followerCount = followerCount ?: 0L,
    trackCount = trackCount ?: 0L,
    isVerified = isVerified == true,
)

fun TrackDto.toDomainOrNull(): Track? {
    val trackId = id ?: return null
    return Track(
        id = trackId,
        title = title?.takeIf { it.isNotBlank() } ?: "Untitled",
        artist = user?.toDomain() ?: UnknownArtist,
        artworkUrl = artwork.toDomain()?.bestUrl(),
        genre = genre,
        mood = mood,
        durationSeconds = duration ?: 0,
        playCount = playCount ?: 0L,
        favoriteCount = favoriteCount ?: 0L,
        repostCount = repostCount ?: 0L,
        commentCount = commentCount ?: 0L,
        description = description,
        isStreamable = isStreamable != false,
        releaseDate = releaseDate,
        permalink = permalink,
    )
}

fun PlaylistDto.toDomain(): Playlist = Playlist(
    id = id ?: playlistId.orEmpty(),
    name = playlistName?.takeIf { it.isNotBlank() } ?: "Untitled playlist",
    ownerName = user?.toDomain()?.displayName,
    artworkUrl = artwork.toDomain()?.bestUrl(),
    description = description,
    trackCount = trackCount ?: tracks?.size ?: 0,
    playCount = playCount ?: 0L,
    isAlbum = isAlbum == true,
    tracks = tracks.orEmpty().mapNotNull { it.toDomainOrNull() },
)
