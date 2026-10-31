package com.sona.core.data.sound

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class StreamVolume(
    val stream: String,
    val current: Int,
    val max: Int,
)

data class SoundInfo(
    val ringerMode: String,
    val isMusicActive: Boolean,
    val volumes: List<StreamVolume>,
    val outputDevices: List<String>,
)

interface SoundReader {
    suspend fun read(): SoundInfo
}

class AndroidSoundReader(private val context: Context) : SoundReader {

    override suspend fun read(): SoundInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(AudioManager::class.java)

        val streams = listOf(
            AudioManager.STREAM_MUSIC to "Media",
            AudioManager.STREAM_RING to "Ringtone",
            AudioManager.STREAM_ALARM to "Alarm",
            AudioManager.STREAM_NOTIFICATION to "Notification",
            AudioManager.STREAM_VOICE_CALL to "Call",
            AudioManager.STREAM_SYSTEM to "System",
        )

        val volumes = streams.map { (stream, label) ->
            StreamVolume(
                stream = label,
                current = runCatching { manager?.getStreamVolume(stream) ?: 0 }.getOrDefault(0),
                max = runCatching { manager?.getStreamMaxVolume(stream) ?: 0 }.getOrDefault(0),
            )
        }

        val outputs = runCatching {
            manager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS).orEmpty().map { it.typeLabel() }
        }.getOrDefault(emptyList())

        SoundInfo(
            ringerMode = when (manager?.ringerMode) {
                AudioManager.RINGER_MODE_SILENT -> "Silent"
                AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
                AudioManager.RINGER_MODE_NORMAL -> "Normal"
                else -> "Unknown"
            },
            isMusicActive = manager?.isMusicActive ?: false,
            volumes = volumes,
            outputDevices = outputs.distinct(),
        )
    }

    private fun AudioDeviceInfo.typeLabel(): String = when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Speaker"
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Earpiece"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired headphones"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired headset"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB headset"
        AudioDeviceInfo.TYPE_HDMI -> "HDMI"
        else -> "Output ${type}"
    }
}
