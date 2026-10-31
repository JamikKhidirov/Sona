package com.sona.feature.splashscreen.impl.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sona.core.ui.component.SonaLogo

/** Animated brand block shown while the app warms up. */
@Composable
fun SplashBrand(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SonaLogo(size = 96.dp)
        Spacer(Modifier.height(26.dp))
        Text(
            text = "SONA",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Open music, beautifully played.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(38.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(26.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.5.dp,
        )
    }
}
