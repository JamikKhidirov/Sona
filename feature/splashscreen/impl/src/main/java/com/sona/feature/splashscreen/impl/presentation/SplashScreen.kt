package com.sona.feature.splashscreen.impl.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.sona.core.ui.component.SonaBackground
import com.sona.feature.splashscreen.impl.presentation.components.SplashBrand
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnFinished = rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        delay(1_100L)
        latestOnFinished.value()
    }

    SonaBackground(modifier = modifier.fillMaxSize()) {
        SplashBrand(modifier = Modifier.fillMaxSize())
    }
}
