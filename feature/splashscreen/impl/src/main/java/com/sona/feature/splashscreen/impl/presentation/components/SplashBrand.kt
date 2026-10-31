package com.sona.feature.splashscreen.impl.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sona.core.ui.component.SonaLogo

/**
 * Splash brand block: the app avatar (the same mark the launcher icon uses)
 * springs in, then breathes while the vector glow pulses behind it.
 *
 * All motion is decorative and finishes or loops without blocking the 1.1 s
 * navigation timer in [com.sona.feature.splashscreen.impl.presentation.SplashScreen].
 */
@Composable
fun SplashBrand(modifier: Modifier = Modifier) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    // Entrance: 0.82 → 1.0 with a soft spring, alpha 0 → 1.
    val enterScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.82f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessLow,
        ),
        label = "avatarEnterScale",
    )
    val enterAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "avatarEnterAlpha",
    )

    // Idle: a 4.5 % breathing loop, 2× slower than the equalizer.
    val breathe = rememberInfiniteTransition(label = "avatarBreathe")
    val pulse by breathe.animateFloat(
        initialValue = 1f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "avatarPulse",
    )

    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Vector glow, slightly larger than the avatar, pulsing with it.
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        scaleX = enterScale * pulse
                        scaleY = enterScale * pulse
                        alpha = enterAlpha * 0.55f
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            SonaLogo(
                size = 108.dp,
                modifier = Modifier.graphicsLayer {
                    scaleX = enterScale * pulse
                    scaleY = enterScale * pulse
                    alpha = enterAlpha
                },
            )
        }

        Spacer(Modifier.height(30.dp))
        Text(
            text = "SONA",
            style = MaterialTheme.typography.displayLarge,
            letterSpacing = 6.sp,
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
