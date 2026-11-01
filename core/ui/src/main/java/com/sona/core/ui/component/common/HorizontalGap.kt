package com.sona.core.ui.component.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp


@Composable
fun HorizontalGap(width: Dp) {
    Spacer(Modifier.width(width))
}