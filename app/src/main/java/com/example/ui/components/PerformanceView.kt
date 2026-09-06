package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SystemPerformanceState
import com.example.ui.theme.CpuGreen
import com.example.ui.theme.HeatmapExtreme
import com.example.ui.theme.HeatmapMed
import com.example.ui.theme.MemoryPurple
import com.example.ui.theme.Slate900
import com.example.ui.theme.WinBlue

@Composable
fun PerformanceView(
    performance: SystemPerformanceState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. CPU PERFORMANCE CARD (Classic Windows Task Manager Green Graph)
        val freqSubtitle = if (performance.cpuFrequencyGhz > 0f) {
            "${performance.cpuCores} ядер • ${String.format("%.2f", performance.cpuFrequencyGhz)} ГГц"
        } else {
            "${performance.cpuCores} ядер"
        }

        PerformanceChartCard(
            title = "ЦП (Процессор)",
            subtitle = "$freqSubtitle • Использование: ${String.format("%.1f", performance.totalCpuUsage)}%",
            currentValue = "${String.format("%.1f", performance.totalCpuUsage)}%",
            history = performance.cpuHistory,
            lineColor = CpuGreen,
            badgeIcon = Icons.Default.Speed,
            stats = buildList {
                add(Pair("Использование", "${String.format("%.1f", performance.totalCpuUsage)}%"))
                if (performance.cpuFrequencyGhz > 0f) {
                    add(Pair("Частота ядер", "${String.format("%.2f", performance.cpuFrequencyGhz)} ГГц"))
                }
                add(Pair("Ядер ЦП", "${performance.cpuCores}"))
                add(Pair("Процессов", "${performance.activeProcessesCount}"))
                add(Pair("Потоков", "${performance.totalThreadsCount}"))
                add(Pair("Время работы", performance.uptimeFormatted))
            }
        )

        // 2. MEMORY (RAM) CARD (Windows Task Manager Purple Graph)
        val ramUsedGb = performance.memoryUsedMb / 1024f
        val ramTotalGb = performance.memoryTotalMb / 1024f
        val ramPercent = performance.memoryUsagePercentage

        PerformanceChartCard(
            title = "Память (ОЗУ)",
            subtitle = "${String.format("%.1f", ramUsedGb)} ГБ из ${String.format("%.1f", ramTotalGb)} ГБ (${String.format("%.0f", ramPercent)}%)",
            currentValue = "${String.format("%.0f", ramPercent)}%",
            history = performance.memoryHistory,
            lineColor = MemoryPurple,
            badgeIcon = Icons.Default.Memory,
            stats = listOf(
                Pair("Занято", "${String.format("%.1f", ramUsedGb)} ГБ"),
                Pair("Доступно", "${String.format("%.1f", performance.memoryAvailableMb / 1024f)} ГБ"),
                Pair("Всего ОЗУ", "${String.format("%.1f", ramTotalGb)} ГБ"),
                Pair("Порог нехватки", "${String.format("%.0f", performance.memoryThresholdBytes / (1024f * 1024f))} МБ"),
                Pair("Статус памяти", if (performance.isLowMemory) "Критический" else "Норма")
            )
        )

        // 3. BATTERY & THERMAL SENSORS CARD (Hardware Telemetry)
        val tempColor = when {
            performance.batteryTemperatureCelsius >= 40.0f -> HeatmapExtreme
            performance.batteryTemperatureCelsius >= 36.0f -> HeatmapMed
            else -> CpuGreen
        }

        PerformanceChartCard(
            title = "Батарея и Температура",
            subtitle = "${performance.batteryLevelPercent}% • ${String.format("%.1f", performance.batteryTemperatureCelsius)}°C (${performance.thermalStatus})",
            currentValue = "${String.format("%.1f", performance.batteryTemperatureCelsius)}°C",
            history = performance.temperatureHistory,
            lineColor = tempColor,
            badgeIcon = if (performance.isOverheating) Icons.Default.Whatshot else (if (performance.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd),
            stats = buildList {
                add(Pair("Температура", "${String.format("%.1f", performance.batteryTemperatureCelsius)}°C"))
                add(Pair("Заряд", "${performance.batteryLevelPercent}%"))
                add(Pair("Статус питания", if (performance.isCharging) "Зарядка" else "Разрядка"))
                if (performance.batteryVoltageMv > 0) {
                    add(Pair("Напряжение", "${performance.batteryVoltageMv} мВ"))
                }
                add(Pair("Нагрев", performance.thermalStatus))
                if (performance.thermalCulpritProcess != null) {
                    add(Pair("Главный источник нагрева", performance.thermalCulpritProcess.appName))
                }
            }
        )

        // 4. SYSTEM & HARDWARE SPECS
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WinBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Сведения о системе",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SpecRow("Устройство", performance.deviceModel)
                SpecRow("Операционная система", performance.androidVersion)
                SpecRow("Логических процессоров", "${performance.cpuCores} cores")
                SpecRow("Общее время работы", performance.uptimeFormatted)
                if (performance.dataSourceDescription.isNotEmpty()) {
                    SpecRow("Режим телеметрии", performance.dataSourceDescription)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PerformanceChartCard(
    title: String,
    subtitle: String,
    currentValue: String,
    history: List<Float>,
    lineColor: Color,
    badgeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    stats: List<Pair<String, String>>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(lineColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = lineColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = lineColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Graph Canvas (Windows Task Manager Grid and Waveform)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate900)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                WindowsTaskChartCanvas(
                    data = history,
                    lineColor = lineColor,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detail Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stats.take(3).forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (stats.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    stats.drop(3).forEach { (label, value) ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WindowsTaskChartCanvas(
    data: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Draw Windows Task Manager Grid Lines
        val gridCols = 10
        val gridRows = 5
        val gridColor = Color(0x1838BDF8)

        for (i in 0..gridCols) {
            val x = (w / gridCols) * i
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
        }
        for (j in 0..gridRows) {
            val y = (h / gridRows) * j
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        if (data.isEmpty()) return@Canvas

        // Prepare points
        val pointsCount = data.size
        val stepX = if (pointsCount > 1) w / (pointsCount - 1) else w

        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val normalizedY = (100f - value.coerceIn(0f, 100f)) / 100f
            val x = index * stepX
            val y = normalizedY * (h - 8f) + 4f

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == data.size - 1) {
                fillPath.lineTo(x, h)
                fillPath.close()
            }
        }

        // Draw area gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)),
                startY = 0f,
                endY = h
            )
        )

        // Draw crisp curve line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // Draw glowing point at the last data position
        val lastVal = data.last().coerceIn(0f, 100f)
        val lastX = (data.size - 1) * stepX
        val lastY = ((100f - lastVal) / 100f) * (h - 8f) + 4f
        drawCircle(
            color = lineColor,
            radius = 4.5f,
            center = Offset(lastX, lastY)
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
