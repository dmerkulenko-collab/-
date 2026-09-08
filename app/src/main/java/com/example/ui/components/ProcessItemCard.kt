package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessImportanceCategory
import com.example.model.ProcessInfo
import com.example.ui.theme.CpuGreen
import com.example.ui.theme.HeatmapExtreme
import com.example.ui.theme.HeatmapHigh
import com.example.ui.theme.HeatmapLow
import com.example.ui.theme.HeatmapMed
import com.example.ui.theme.WinBlue
import com.example.ui.theme.MemoryPurple

@Composable
fun ProcessItemCard(
    process: ProcessInfo,
    onKillClick: (ProcessInfo) -> Unit,
    onDetailsClick: (ProcessInfo) -> Unit,
    onOpenSettingsClick: (ProcessInfo) -> Unit,
    onLaunchClick: (ProcessInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Color code based on CPU load
    val (cpuBgColor, cpuTextColor) = when {
        process.cpuPercentage >= 40f -> Pair(HeatmapExtreme.copy(alpha = 0.22f), HeatmapExtreme)
        process.cpuPercentage >= 20f -> Pair(HeatmapHigh.copy(alpha = 0.20f), HeatmapHigh)
        process.cpuPercentage >= 5f -> Pair(HeatmapMed.copy(alpha = 0.18f), HeatmapMed)
        else -> Pair(HeatmapLow.copy(alpha = 0.12f), HeatmapLow)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isExpanded) 3.dp else 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (process.cpuPercentage >= 30f) 1.5.dp else 0.5.dp,
                color = if (process.cpuPercentage >= 30f) HeatmapExtreme.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { isExpanded = !isExpanded }
            .testTag("process_card_${process.pid}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (process.icon != null) {
                        DrawableImage(
                            drawable = process.icon,
                            contentDescription = process.appName,
                            modifier = Modifier.size(34.dp)
                        )
                    } else {
                        Text(
                            text = process.appName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name, PID, and State
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = process.appName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (process.isGame) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFF5722).copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "🎮 Игра",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFFFF5722),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (process.isSystemApp) {
                            Text(
                                text = " [Сис]",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PID: ${process.pid}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Status Badge
                        val (statusText, statusColor) = when (process.importanceCategory) {
                            ProcessImportanceCategory.FOREGROUND -> Pair("Активен", CpuGreen)
                            ProcessImportanceCategory.VISIBLE -> Pair("Видим", WinBlue)
                            ProcessImportanceCategory.SERVICE -> Pair("Служба", MemoryPurple)
                            ProcessImportanceCategory.BACKGROUND -> Pair("Фон", MaterialTheme.colorScheme.outline)
                            ProcessImportanceCategory.CACHED -> Pair("Кэш", MaterialTheme.colorScheme.outline)
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        if (process.powerUsageLevel != "Низкое") {
                            val powerColor = when (process.powerUsageLevel) {
                                "Очень высокое" -> HeatmapExtreme
                                "Высокое" -> HeatmapHigh
                                else -> HeatmapMed
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = powerColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "⚡ ${process.powerUsageLevel}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = powerColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        if (!process.isMeasurementReal) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "SELinux",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // CPU Heatmap Badge - KEY FOCUS!
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = cpuBgColor,
                    modifier = Modifier.width(66.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${process.cpuPercentage}%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            ),
                            color = cpuTextColor
                        )
                        Text(
                            text = "ЦП",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = cpuTextColor.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // RAM Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.width(64.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${process.memoryMb.toInt()} МБ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ОЗУ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // Expandable Action Drawer (Windows "End Task", "Details", "Settings")
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Пакет: ${process.packageName} | Потоков: ${process.threadCount} | Энергия: ${process.powerUsageLevel} (Индекс: ${process.thermalImpactScore}/100)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "Снять задачу" (End Task)
                        Button(
                            onClick = { onKillClick(process) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HeatmapExtreme.copy(alpha = 0.9f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("kill_btn_${process.pid}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Снять задачу",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // App Settings (Force Stop in Android)
                        OutlinedButton(
                            onClick = { onOpenSettingsClick(process) },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_btn_${process.pid}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Инфо",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Full Details Modal
                        IconButton(
                            onClick = { onDetailsClick(process) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .size(40.dp)
                                .testTag("details_btn_${process.pid}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Подробнее о процессе",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawableImage(
    drawable: Drawable?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (drawable == null) {
        Box(modifier = modifier)
        return
    }

    val bitmap = remember(drawable) {
        try {
            val width = if (drawable.intrinsicWidth in 1..2048) drawable.intrinsicWidth else 72
            val height = if (drawable.intrinsicHeight in 1..2048) drawable.intrinsicHeight else 72
            val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bm
        } catch (_: Throwable) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier)
    }
}
