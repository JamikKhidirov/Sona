package com.sona.core.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SensorEntry(
    val name: String,
    val vendor: String,
    val type: Int,
    val typeLabel: String,
    val powerMilliAmp: Float,
    val resolution: Float,
    val maxRange: Float,
    val isWakeUp: Boolean,
)

data class SensorsInfo(
    val count: Int,
    val sensors: List<SensorEntry>,
)

interface SensorsReader {
    suspend fun read(): SensorsInfo
}

class AndroidSensorsReader(private val context: Context) : SensorsReader {

    override suspend fun read(): SensorsInfo = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(SensorManager::class.java)
        val sensors = manager?.getSensorList(Sensor.TYPE_ALL).orEmpty().map { sensor ->
            SensorEntry(
                name = sensor.name,
                vendor = sensor.vendor,
                type = sensor.type,
                typeLabel = sensor.stringType.orEmpty(),
                powerMilliAmp = sensor.power,
                resolution = sensor.resolution,
                maxRange = sensor.maximumRange,
                isWakeUp = sensor.isWakeUpSensor,
            )
        }
        SensorsInfo(count = sensors.size, sensors = sensors)
    }
}
