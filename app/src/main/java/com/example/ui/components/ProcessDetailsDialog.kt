package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProcessInfo
import com.example.ui.theme.HeatmapExtreme
import com.example.ui.theme.WinBlue

@Composable
fun ProcessDetailsDialog(
    process: ProcessInfo,
    onDismiss: () -> Unit,
    onKill: (ProcessInfo) -> Unit,
    onOpenSettings: (ProcessInfo) -> Unit,
    onLaunch: (ProcessInfo) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (process.icon != null) {
                        DrawableImage(
                            drawable = process.icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Text(
                            text = process.appName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Column {
                    Text(
                        text = process.appName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Свойства процесса",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DetailItem("Идентификатор (PID)", "${process.pid}")
                DetailItem("Идентификатор польз. (UID)", "${process.uid}")
                DetailItem("Нагрузка на ЦП", "${process.cpuPercentage}%")
                DetailItem("Использование ОЗУ", "${String.format("%.1f", process.memoryMb)} МБ (${process.memoryBytes} байт)")
                DetailItem("Состояние", process.importanceCategory.getDisplayNameRussian())
                DetailItem("Потоков выполнения", "${process.threadCount}")
                DetailItem("Тип приложения", if (process.isSystemApp) "Системное" else "Пользовательское")
                DetailItem("Имя пакета", process.packageName)
                DetailItem("Версия", process.appVersion)

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons inside Dialog
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            onKill(process)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HeatmapExtreme),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_kill_process_btn")
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Снять задачу (Завершить процесс)", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            onOpenSettings(process)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_open_settings_btn")
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Остановить в настройках Android")
                    }

                    OutlinedButton(
                        onClick = {
                            onLaunch(process)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Открыть приложение")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_close_btn")
            ) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
