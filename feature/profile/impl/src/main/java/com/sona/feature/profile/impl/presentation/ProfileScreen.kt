package com.sona.feature.profile.impl.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.ui.component.SectionHeader
import com.sona.feature.profile.impl.presentation.components.ConnectAudiusCard
import com.sona.feature.profile.impl.presentation.components.ProfileHero
import com.sona.feature.profile.impl.presentation.components.ProfileMenuItem
import com.sona.feature.profile.impl.presentation.components.ProfileTracksRow

@Composable
fun ProfileRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onOpenTrack: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavorites: () -> Unit,
) {
    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onOpenTrack = onOpenTrack,
        onOpenSettings = onOpenSettings,
        onOpenFavorites = onOpenFavorites,
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onOpenTrack: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavorites: () -> Unit,
) {
    var showAbout by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item("header") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                if (state.isSignedIn) {
                    IconButton(onClick = { onIntent(ProfileIntent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item("hero") {
            ProfileHero(state = state)
        }

        if (!state.isSignedIn) {
            item("connect") {
                ConnectAudiusCard(onOpenSettings = onOpenSettings)
            }
        }

        if (state.recentlyPlayed.isNotEmpty()) {
            item("recent-header") {
                SectionHeader(title = "Recently played")
            }
            item("recent-list") {
                ProfileTracksRow(
                    tracks = state.recentlyPlayed,
                    currentTrackId = state.currentTrackId,
                    isPlaying = state.isPlaying,
                    onOpenTrack = onOpenTrack,
                    onPlay = { onIntent(ProfileIntent.PlayTrack(it)) },
                )
            }
        }

        if (state.favorites.isNotEmpty()) {
            item("favorites-header") {
                SectionHeader(
                    title = "Liked tracks",
                    action = "Open",
                    onAction = onOpenFavorites,
                )
            }
            item("favorites-list") {
                ProfileTracksRow(
                    tracks = state.favorites.take(12),
                    currentTrackId = state.currentTrackId,
                    isPlaying = state.isPlaying,
                    onOpenTrack = onOpenTrack,
                    onPlay = { onIntent(ProfileIntent.PlayTrack(it)) },
                )
            }
        }

        item("menu") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileMenuItem(
                    icon = Icons.Rounded.Favorite,
                    title = "Your library",
                    subtitle = "${state.favorites.size} liked tracks",
                    onClick = onOpenFavorites,
                )
                ProfileMenuItem(
                    icon = Icons.Rounded.Settings,
                    title = "Settings",
                    subtitle = "Theme, Audius account, storage",
                    onClick = onOpenSettings,
                )
                ProfileMenuItem(
                    icon = Icons.Rounded.Info,
                    title = "About Sona",
                    subtitle = "Open music, beautifully played",
                    onClick = { showAbout = true },
                )
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Close") }
            },
            title = { Text("Sona") },
            text = {
                Text(
                    "A Compose-first music player powered by the Audius open network. " +
                        "Streaming is provided by Audius and its artists.",
                )
            },
        )
    }
}
