package com.sona.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sona.core.audio.impl.service.PlaybackService
import com.sona.feature.navigation.impl.presentation.SonaApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()
        keepPlaybackServiceAlive()

        val factory = (application as SonaApplication).appComponent.viewModelFactory()
        setContent {
            SonaApp(viewModelFactory = factory)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST,
            )
        }
    }

    private fun keepPlaybackServiceAlive() {
        runCatching {
            startService(Intent(this, PlaybackService::class.java))
        }
    }

    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST = 1001
    }
}
