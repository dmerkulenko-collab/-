package com.example.model

data class SystemPerformanceState(
    val totalCpuUsage: Float = 0f, // 0..100%
    val cpuHistory: List<Float> = emptyList(), // last N samples
    val memoryUsedBytes: Long = 0L,
    val memoryTotalBytes: Long = 0L,
    val memoryAvailableBytes: Long = 0L,
    val memoryThresholdBytes: Long = 0L,
    val isLowMemory: Boolean = false,
    val memoryHistory: List<Float> = emptyList(),
    val activeProcessesCount: Int = 0,
    val totalThreadsCount: Int = 0,
    val uptimeFormatted: String = "0:00:00",
    val cpuCores: Int = 1,
    val deviceModel: String = "",
    val androidVersion: String = "",
    val topCpuProcess: ProcessInfo? = null
) {
    val memoryUsagePercentage: Float
        get() = if (memoryTotalBytes > 0) {
            (memoryUsedBytes.toFloat() / memoryTotalBytes.toFloat()) * 100f
        } else 0f

    val memoryUsedMb: Float
        get() = memoryUsedBytes / (1024f * 1024f)

    val memoryTotalMb: Float
        get() = memoryTotalBytes / (1024f * 1024f)

    val memoryAvailableMb: Float
        get() = memoryAvailableBytes / (1024f * 1024f)
}
