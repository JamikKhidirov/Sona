package com.sona.core.data.performance

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

data class PerformanceInfo(
    val cpuUsagePercent: Float,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val usedRamBytes: Long,
    val heapUsedBytes: Long,
    val heapMaxBytes: Long,
    val threadCount: Int,
    val uptimeMinutes: Long,
)

interface PerformanceReader {
    suspend fun read(): PerformanceInfo
}

class AndroidPerformanceReader(private val context: Context) : PerformanceReader {

    override suspend fun read(): PerformanceInfo = withContext(Dispatchers.IO) {
        val first = readCpuTicks()
        delay(200L)
        val second = readCpuTicks()
        val cpuUsage = if (first != null && second != null) {
            val totalDelta = second.total - first.total
            val idleDelta = second.idle - first.idle
            if (totalDelta > 0) {
                ((totalDelta - idleDelta).toFloat() / totalDelta * 100f).coerceIn(0f, 100f)
            } else {
                0f
            }
        } else {
            0f
        }

        val activityManager = context.getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo()
        runCatching { activityManager?.getMemoryInfo(memoryInfo) }

        val runtime = Runtime.getRuntime()

        PerformanceInfo(
            cpuUsagePercent = cpuUsage,
            totalRamBytes = memoryInfo.totalMem,
            availableRamBytes = memoryInfo.availMem,
            usedRamBytes = (memoryInfo.totalMem - memoryInfo.availMem).coerceAtLeast(0L),
            heapUsedBytes = runtime.totalMemory() - runtime.freeMemory(),
            heapMaxBytes = runtime.maxMemory(),
            threadCount = Thread.activeCount(),
            uptimeMinutes = SystemClock.elapsedRealtime() / 60_000L,
        )
    }

    private data class CpuTicks(val total: Long, val idle: Long)

    private fun readCpuTicks(): CpuTicks? = runCatching {
        val firstLine = File("/proc/stat").readLines().first { it.startsWith("cpu ") }
        val parts = firstLine.trim().split(Regex("\\s+")).drop(1).map { it.toLong() }
        val idle = parts.getOrElse(3) { 0L } + parts.getOrElse(4) { 0L }
        CpuTicks(total = parts.sum(), idle = idle)
    }.getOrNull()
}
