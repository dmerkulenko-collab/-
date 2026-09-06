package com.example.model

import android.graphics.drawable.Drawable

data class AppHistoryUsage(
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMs: Long,
    val lastTimeUsed: Long,
    val isSystemApp: Boolean,
    val icon: Drawable? = null
) {
    val formattedForegroundTime: String
        get() {
            val totalSeconds = totalTimeInForegroundMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return when {
                hours > 0 -> "${hours} ч ${minutes} мин"
                minutes > 0 -> "${minutes} мин ${seconds} сек"
                else -> "${seconds} сек"
            }
        }
}
