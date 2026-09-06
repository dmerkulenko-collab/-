package com.example.model

import android.graphics.drawable.Drawable

enum class ProcessImportanceCategory {
    FOREGROUND,
    VISIBLE,
    SERVICE,
    BACKGROUND,
    CACHED;

    fun getDisplayNameRussian(): String = when (this) {
        FOREGROUND -> "На переднем плане"
        VISIBLE -> "Видимый"
        SERVICE -> "Фоновая служба"
        BACKGROUND -> "Фоновый процесс"
        CACHED -> "Кэшированный"
    }
}

data class ProcessInfo(
    val packageName: String,
    val appName: String,
    val pid: Int,
    val uid: Int,
    val cpuPercentage: Float, // e.g. 34.5%
    val memoryBytes: Long,
    val memoryMb: Float,
    val memoryPercentage: Float,
    val importanceCategory: ProcessImportanceCategory,
    val isSystemApp: Boolean,
    val isGame: Boolean = false,
    val threadCount: Int,
    val appVersion: String,
    val icon: Drawable? = null,
    val isMeasurementReal: Boolean = true,
    val realCpuTicks: Long = 0L,
    val foregroundTimeTodayMinutes: Long = 0L,
    val lastUsedTimestamp: Long = 0L,
    val powerUsageLevel: String = "Низкое",
    val thermalImpactScore: Int = 0
)
