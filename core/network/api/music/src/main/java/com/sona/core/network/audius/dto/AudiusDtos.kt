package com.sona.core.network.audius.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class AudiusEnvelope<T>(
    @SerializedName("data") val data: T? = null,
)

data class ArtworkDto(
    @SerializedName("150x150") val small: String? = null,
    @SerializedName("480x480") val medium: String? = null,
    @SerializedName("1000x1000") val large: String? = null,
)

data class UserDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("handle") val handle: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("profile_picture") val profilePicture: ArtworkDto? = null,
    @SerializedName("cover_photo") val coverPhoto: ArtworkDto? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("follower_count") val followerCount: Long? = null,
    @SerializedName("followee_count") val followeeCount: Long? = null,
    @SerializedName("track_count") val trackCount: Long? = null,
    @SerializedName("playlist_count") val playlistCount: Long? = null,
    @SerializedName("is_verified") val isVerified: Boolean? = null,
    @SerializedName("location") val location: String? = null,
)

data class TrackDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("artwork") val artwork: ArtworkDto? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("mood") val mood: String? = null,
    @SerializedName("duration") val duration: Int? = null,
    @SerializedName("play_count") val playCount: Long? = null,
    @SerializedName("favorite_count") val favoriteCount: Long? = null,
    @SerializedName("repost_count") val repostCount: Long? = null,
    @SerializedName("comment_count") val commentCount: Long? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("is_streamable") val isStreamable: Boolean? = null,
    @SerializedName("is_downloadable") val isDownloadable: Boolean? = null,
    @SerializedName("has_current_user_favorited") val isFavorite: Boolean? = null,
    @SerializedName("has_current_user_reposted") val isReposted: Boolean? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("permalink") val permalink: String? = null,
    @SerializedName("tags") val tags: String? = null,
)

data class PlaylistDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("playlist_id") val playlistId: String? = null,
    @SerializedName("playlist_name") val playlistName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("artwork") val artwork: ArtworkDto? = null,
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("track_count") val trackCount: Int? = null,
    @SerializedName("total_play_count") val playCount: Long? = null,
    @SerializedName("is_album") val isAlbum: Boolean? = null,
    @SerializedName("is_private") val isPrivate: Boolean? = null,
    @SerializedName("permalink") val permalink: String? = null,
    @SerializedName("tracks") val tracks: List<TrackDto>? = null,
)

data class CommentDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("entity_id") val entityId: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("user") val user: UserDto? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("reply_count") val replyCount: Int? = null,
    @SerializedName("is_edited") val isEdited: Boolean? = null,
    @SerializedName("is_current_user_reacted") val isReacted: Boolean? = null,
    @SerializedName("react_count") val reactCount: Int? = null,
)

data class CreateCommentRequest(
    @SerializedName("message") val message: String,
    @SerializedName("parent_id") val parentId: String? = null,
)

data class UpdateTrackRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("mood") val mood: String? = null,
    @SerializedName("tags") val tags: String? = null,
    @SerializedName("is_unlisted") val isUnlisted: Boolean? = null,
    @SerializedName("is_streamable") val isStreamable: Boolean? = null,
)

data class UpdatePlaylistRequest(
    @SerializedName("playlist_name") val playlistName: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("is_private") val isPrivate: Boolean? = null,
    @SerializedName("is_album") val isAlbum: Boolean? = null,
)

data class TrackIdDto(
    @SerializedName("track_id") val trackId: String? = null,
)

data class CommentResponse(
    @SerializedName("data") val data: CommentDto? = null,
)

data class JsonEnvelope(
    @SerializedName("data") val data: JsonElement? = null,
)
