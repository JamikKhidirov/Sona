package com.sona.feature.comments.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.common.domain.usecase.music.GetTrackComments
import com.sona.core.common.domain.usecase.music.PostTrackComment
import com.sona.core.common.domain.usecase.settings.ObserveSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CommentsViewModel @Inject constructor(
    private val getTrackComments: GetTrackComments,
    private val postTrackComment: PostTrackComment,
    observeSettings: ObserveSettings,
) : ViewModel() {

    private val _state = MutableStateFlow(CommentsUiState())
    val state: StateFlow<CommentsUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CommentsEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<CommentsEffect> = _effects.asSharedFlow()

    private var loadJob: Job? = null

    init {
        observeSettings()
            .onEach { settings -> _state.update { it.copy(isSignedIn = settings.isSignedIn) } }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: CommentsIntent) {
        when (intent) {
            is CommentsIntent.Load -> {
                if (_state.value.trackId != intent.trackId || _state.value.comments.isEmpty()) {
                    load(intent.trackId)
                }
            }
            CommentsIntent.Retry -> load(_state.value.trackId)
            is CommentsIntent.DraftChanged -> _state.update { it.copy(draft = intent.text) }
            CommentsIntent.Submit -> submit()
        }
    }

    private fun load(trackId: String) {
        if (trackId.isBlank()) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(trackId = trackId, isLoading = true, error = null) }
            getTrackComments(trackId)
                .onSuccess { comments ->
                    _state.update { it.copy(isLoading = false, comments = comments) }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Не удалось загрузить комментарии.",
                        )
                    }
                }
        }
    }

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) {
            if (!current.isSignedIn) {
                viewModelScope.launch {
                    _effects.emit(
                        CommentsEffect.Message(
                            "Добавьте токен Audius в настройках, чтобы комментировать",
                        ),
                    )
                }
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isPosting = true) }
            postTrackComment(current.trackId, current.draft)
                .onSuccess { comment ->
                    _state.update {
                        it.copy(
                            isPosting = false,
                            draft = "",
                            comments = listOf(comment) + it.comments,
                        )
                    }
                    _effects.emit(CommentsEffect.Message("Комментарий опубликован"))
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isPosting = false) }
                    _effects.emit(
                        CommentsEffect.Message(
                            throwable.message ?: "Не удалось отправить комментарий",
                        ),
                    )
                }
        }
    }
}
