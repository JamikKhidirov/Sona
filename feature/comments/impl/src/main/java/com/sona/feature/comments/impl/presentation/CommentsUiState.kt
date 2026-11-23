package com.sona.feature.comments.impl.presentation

import com.sona.core.common.model.Comment

data class CommentsUiState(
    val trackId: String = "",
    val isLoading: Boolean = true,
    val comments: List<Comment> = emptyList(),
    val draft: String = "",
    val isPosting: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = draft.isNotBlank() && !isPosting && isSignedIn
}

sealed interface CommentsIntent {
    data class Load(val trackId: String) : CommentsIntent
    data object Retry : CommentsIntent
    data class DraftChanged(val text: String) : CommentsIntent
    data object Submit : CommentsIntent
}

sealed interface CommentsEffect {
    data class Message(val text: String) : CommentsEffect
}
