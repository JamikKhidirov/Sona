package com.sona.feature.artistcommunity.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sona.core.common.domain.usecase.music.GetArtist
import com.sona.core.common.domain.usecase.music.GetArtistFollowers
import com.sona.core.common.domain.usecase.music.GetArtistFollowing
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

class ArtistCommunityViewModel @Inject constructor(
    private val getArtist: GetArtist,
    private val getArtistFollowers: GetArtistFollowers,
    private val getArtistFollowing: GetArtistFollowing,
) : ViewModel() {

    private val _state = MutableStateFlow(ArtistCommunityUiState())
    val state: StateFlow<ArtistCommunityUiState> = _state.asStateFlow()

    private var lastArtistId: String? = null
    private var loadJob: Job? = null

    fun onIntent(intent: ArtistCommunityIntent) {
        when (intent) {
            is ArtistCommunityIntent.Load -> {
                if (lastArtistId != intent.artistId) {
                    lastArtistId = intent.artistId
                    load(intent.artistId)
                }
            }
            ArtistCommunityIntent.Retry -> lastArtistId?.let(::load)
            is ArtistCommunityIntent.SelectTab -> _state.update { it.copy(tab = intent.tab) }
        }
    }

    private fun load(artistId: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(artistId = artistId, isLoading = true, error = null) }
            supervisorScope {
                val artistDeferred = async { getArtist(artistId) }
                val followersDeferred = async { getArtistFollowers(artistId) }
                val followingDeferred = async { getArtistFollowing(artistId) }

                val artistResult = artistDeferred.await()
                val followersResult = followersDeferred.await()
                val followingResult = followingDeferred.await()

                val allFailed = artistResult.isFailure &&
                    followersResult.isFailure &&
                    followingResult.isFailure

                _state.update {
                    it.copy(
                        isLoading = false,
                        artist = artistResult.getOrNull() ?: it.artist,
                        followers = followersResult.getOrDefault(it.followers),
                        following = followingResult.getOrDefault(it.following),
                        error = if (allFailed) {
                            followersResult.exceptionOrNull()?.message
                                ?: followingResult.exceptionOrNull()?.message
                                ?: "Не удалось загрузить сообщество артиста."
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}
