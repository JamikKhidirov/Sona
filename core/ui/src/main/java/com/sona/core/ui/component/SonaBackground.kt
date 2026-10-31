package com.sona.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun SonaBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.background,
                        colors.background,
                        colors.surface.copy(alpha = 0.72f),
                    ),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawGlow(
                color = colors.primary,
                center = Offset(size.width * 0.92f, size.height * 0.03f),
                radius = size.width * 0.82f,
                alpha = 0.24f,
            )
            drawGlow(
                color = colors.secondary,
                center = Offset(size.width * 0.05f, size.height * 0.38f),
                radius = size.width * 0.62f,
                alpha = 0.13f,
            )
            drawGlow(
                color = colors.tertiary,
                center = Offset(size.width * 0.78f, size.height * 0.92f),
                radius = size.width * 0.7f,
                alpha = 0.08f,
            )
        }
        content()
    }
}

private fun DrawScope.drawGlow(
    color: Color,
    center: Offset,
    radius: Float,
    alpha: Float,
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
