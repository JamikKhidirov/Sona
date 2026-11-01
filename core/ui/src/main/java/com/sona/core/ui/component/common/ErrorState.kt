package com.sona.core.ui.component.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = "Что-то затихло",
        message = message,
        modifier = modifier,
        icon = Icons.Rounded.CloudOff,
        actionText = "Обновить",
        onAction = onRetry,
    )
}
