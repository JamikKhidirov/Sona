package com.sona.core.data.display

import android.content.Context
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

data class DisplayInfo(
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Int,
    val density: Float,
    val diagonalInches: Double,
    val refreshRateHz: Float,
    val isLandscape: Boolean,
    val brightnessPercent: Int?,
)

interface DisplayReader {
    suspend fun read(): DisplayInfo
}

class AndroidDisplayReader(private val context: Context) : DisplayReader {

    override suspend fun read(): DisplayInfo = withContext(Dispatchers.IO) {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        val configuration = context.resources.configuration
        val display = runCatching {
            context.getSystemService(DisplayManager::class.java)?.getDisplay(Display.DEFAULT_DISPLAY)
        }.getOrNull()

        val realWidth: Int
        val realHeight: Int
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val bounds = runCatching {
                context.getSystemService(WindowManager::class.java)?.currentWindowMetrics?.bounds
            }.getOrNull()
            realWidth = bounds?.width() ?: metrics.widthPixels
            realHeight = bounds?.height() ?: metrics.heightPixels
        } else {
            realWidth = metrics.widthPixels
            realHeight = metrics.heightPixels
        }

        val widthInches = realWidth / metrics.xdpi.toDouble()
        val heightInches = realHeight / metrics.ydpi.toDouble()

        DisplayInfo(
            widthPx = realWidth,
            heightPx = realHeight,
            densityDpi = metrics.densityDpi,
            density = metrics.density,
            diagonalInches = sqrt(widthInches * widthInches + heightInches * heightInches),
            refreshRateHz = display?.refreshRate ?: 0f,
            isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
            brightnessPercent = runCatching {
                android.provider.Settings.System.getInt(
                    context.contentResolver,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS,
                ) * 100 / 255
            }.getOrNull(),
        )
    }
}
