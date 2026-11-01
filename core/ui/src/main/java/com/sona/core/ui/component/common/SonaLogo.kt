package com.sona.core.ui.component.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sona.core.ui.R


/**
 * The Sona avatar — the same gradient tile with a music note that the launcher
 * icon uses. Every screen (splash, home header, profile) shows this mark so the
 * brand never drifts between the app icon and the in-app logo.
 */
@Composable
fun SonaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Image(
        painter = painterResource(R.drawable.ic_sona_mark),
        contentDescription = "Sona",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f)),
    )
}
