package com.sona.core.common.model

/**
 * A comment left on a track in the Audius network.
 *
 * Audius returns comments as a flat list; replies are represented by
 * [replyCount] and can be loaded separately by parent id in a future iteration.
 */
data class Comment(
    val id: String,
    val message: String,
    val author: Artist,
    val createdAt: String? = null,
    val replyCount: Int = 0,
    val reactCount: Int = 0,
    val isReacted: Boolean = false,
    val isEdited: Boolean = false,
)
