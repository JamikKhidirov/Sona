package com.sona.feature.settings.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sona.core.common.model.ThemeMode
import com.sona.feature.settings.impl.presentation.components.AboutRow
import com.sona.feature.settings.impl.presentation.components.DeviceInfoCard
import com.sona.feature.settings.impl.presentation.components.SettingsActionRow
import com.sona.feature.settings.impl.presentation.components.SettingsSectionLabel
import com.sona.feature.settings.impl.presentation.components.ThemeOption

@Composable
fun SettingsRoute(
    viewModelFactory: ViewModelProvider.Factory,
    onBack: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsEffect.Message -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(SettingsIntent.RefreshDevice)
        onPauseOrDispose { }
    }

    Box(Modifier.fillMaxSize()) {
        SettingsScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onBack = onBack,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )
    }

    if (state.showClearDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SettingsIntent.ClearDialogChanged(false)) },
            title = { Text("Clear your library?") },
            text = {
                Text("Favorites and recently played tracks will be removed from this device.")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.ClearLibrary) }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.ClearDialogChanged(false)) }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
    ) {
        val context = LocalContext.current
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(Modifier.height(20.dp))
        SettingsSectionLabel("Appearance")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThemeOption(
                label = "System",
                icon = Icons.Rounded.PhoneAndroid,
                selected = state.settings.themeMode == ThemeMode.SYSTEM,
                modifier = Modifier.weight(1f),
                onClick = { onIntent(SettingsIntent.ThemeChanged(ThemeMode.SYSTEM)) },
            )
            ThemeOption(
                label = "Light",
                icon = Icons.Rounded.LightMode,
                selected = state.settings.themeMode == ThemeMode.LIGHT,
                modifier = Modifier.weight(1f),
                onClick = { onIntent(SettingsIntent.ThemeChanged(ThemeMode.LIGHT)) },
            )
            ThemeOption(
                label = "Dark",
                icon = Icons.Rounded.DarkMode,
                selected = state.settings.themeMode == ThemeMode.DARK,
                modifier = Modifier.weight(1f),
                onClick = { onIntent(SettingsIntent.ThemeChanged(ThemeMode.DARK)) },
            )
        }

        Spacer(Modifier.height(26.dp))
        SettingsSectionLabel("Audius account")
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = if (state.settings.isSignedIn) "Connected" else "Optional",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (state.settings.isSignedIn) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Paste an Audius OAuth bearer token to access your account. " +
                        "Only needed for profile and account features.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = state.tokenDraft,
                    onValueChange = { onIntent(SettingsIntent.TokenChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API token") },
                    placeholder = { Text("Paste token") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onIntent(SettingsIntent.SaveToken) },
                        enabled = !state.isSavingToken,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text("Save token")
                    }
                    if (state.settings.isSignedIn || state.tokenDraft.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onIntent(SettingsIntent.ClearToken) },
                            shape = RoundedCornerShape(50),
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(26.dp))
        SettingsSectionLabel("This device")
        Spacer(Modifier.height(10.dp))
        DeviceInfoCard(
            device = state.device,
            onManagePermissions = {
                runCatching {
                    context.startActivity(
                        android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.fromParts(
                                "package",
                                context.packageName,
                                null,
                            ),
                        ),
                    )
                }
            },
        )

        Spacer(Modifier.height(26.dp))
        SettingsSectionLabel("Storage")
        Spacer(Modifier.height(10.dp))
        SettingsActionRow(
            icon = Icons.Rounded.DeleteOutline,
            title = "Clear favorites and history",
            subtitle = "Local data only — nothing is deleted from Audius",
            onClick = { onIntent(SettingsIntent.ClearDialogChanged(true)) },
        )

        Spacer(Modifier.height(26.dp))
        SettingsSectionLabel("About")
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AboutRow("Version", "1.0.0")
            AboutRow("Music source", "Audius open network")
            AboutRow("Playback", "Media3 / ExoPlayer")
            AboutRow("API docs", "docs.audius.co")
        }
    }
}
