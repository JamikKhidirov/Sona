package com.sona.core.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sona.core.common.model.ThemeMode

private val OnPrimaryDark = Color(0xFF0F0A1E)

private val DarkColors = darkColorScheme(
    primary = Violet,
    onPrimary = OnPrimaryDark,
    primaryContainer = VioletDeep,
    onPrimaryContainer = TextOnDark,
    secondary = Pink,
    onSecondary = OnPrimaryDark,
    secondaryContainer = Color(0xFF3A1730),
    onSecondaryContainer = Color(0xFFFFD9EC),
    tertiary = Teal,
    onTertiary = Color(0xFF04110F),
    background = Ink,
    onBackground = TextOnDark,
    surface = InkElevated,
    onSurface = TextOnDark,
    surfaceVariant = InkSoft,
    onSurfaceVariant = Color(0xFFBDB6CF),
    outline = Color(0xFF302D3D),
    outlineVariant = Color(0xFF23212E),
    error = ErrorRed,
    onError = Color(0xFF1D0509),
    scrim = Color(0xCC000000),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE4FF),
    onPrimaryContainer = Color(0xFF2A1259),
    secondary = Color(0xFFDB2777),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE1EE),
    onSecondaryContainer = Color(0xFF4A0A26),
    tertiary = Color(0xFF0F766E),
    onTertiary = Color.White,
    background = Mist,
    onBackground = TextOnLight,
    surface = MistElevated,
    onSurface = TextOnLight,
    surfaceVariant = MistSoft,
    onSurfaceVariant = Color(0xFF5D5670),
    outline = Color(0xFFD6CEE6),
    outlineVariant = Color(0xFFE7E1F0),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

@Composable
fun SonaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity() ?: return@SideEffect
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SonaTypography,
        shapes = SonaShapes,
        content = content,
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
