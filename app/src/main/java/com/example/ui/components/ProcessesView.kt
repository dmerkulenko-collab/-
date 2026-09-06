package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.model.SystemPerformanceState
import com.example.ui.theme.CpuGreen
import com.example.ui.theme.HeatmapExtreme
import com.example.ui.theme.HeatmapHigh
import com.example.ui.theme.HeatmapMed
import com.example.viewmodel.ProcessFilter
import com.example.viewmodel.SortField

@Composable
fun ProcessesView(
    processes: List<ProcessInfo>,
    performance: SystemPerformanceState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filter: ProcessFilter,
    onFilterChange: (ProcessFilter) -> Unit,
    sortField: SortField,
    sortAscending: Boolean,
    onSortChange: (SortField) -> Unit,
    onKillProcess: (ProcessInfo) -> Unit,
    onDetailsClick: (ProcessInfo) -> Unit,
    onOpenSettingsClick: (ProcessInfo) -> Unit,
    onLaunchClick: (ProcessInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter and sort items
    val filteredProcesses = processes.filter { process ->
        val matchesSearch = searchQuery.isBlank() ||
                process.appName.contains(searchQuery, ignoreCase = true) ||
                process.packageName.contains(searchQuery, ignoreCase = true) ||
                process.pid.toString().contains(searchQuery)

        val matchesFilter = when (filter) {
            ProcessFilter.ALL -> true
            ProcessFilter.HIGH_CPU -> process.cpuPercentage >= 5.0f
            ProcessFilter.USER_APPS -> !process.isSystemApp
        }

        matchesSearch && matchesFilter
    }.let { list ->
        when (sortField) {
            SortField.CPU -> if (sortAscending) list.sortedBy { it.cpuPercentage } else list.sortedByDescending { it.cpuPercentage }
            SortField.MEMORY -> if (sortAscending) list.sortedBy { it.memoryBytes } else list.sortedByDescending { it.memoryBytes }
            SortField.NAME -> if (sortAscending) list.sortedBy { it.appName.lowercase() } else list.sortedByDescending { it.appName.lowercase() }
            SortField.PID -> if (sortAscending) list.sortedBy { it.pid } else list.sortedByDescending { it.pid }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. TOP CPU CONSUMER HERO BANNER ("Кто грузит процессор")
        val topProcess = performance.topCpuProcess
        if (topProcess != null && topProcess.cpuPercentage >= 2.0f) {
            item(key = "top_cpu_hero") {
                TopCpuBanner(
                    process = topProcess,
                    onKillClick = { onKillProcess(topProcess) },
                    onDetailsClick = { onDetailsClick(topProcess) }
                )
            }
        }

        // 2. Search & Filter Bar
        item(key = "search_filter_bar") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Поиск процесса, приложения или PID...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Очистить поиск"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("process_search_input")
                )

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filter == ProcessFilter.ALL,
                        onClick = { onFilterChange(ProcessFilter.ALL) },
                        label = { Text("Все (${processes.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    val highCpuCount = processes.count { it.cpuPercentage >= 5.0f }
                    FilterChip(
                        selected = filter == ProcessFilter.HIGH_CPU,
                        onClick = { onFilterChange(ProcessFilter.HIGH_CPU) },
                        label = { Text("Высокий ЦП > 5% ($highCpuCount)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = if (filter == ProcessFilter.HIGH_CPU) HeatmapExtreme else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HeatmapExtreme.copy(alpha = 0.18f),
                            selectedLabelColor = HeatmapExtreme
                        )
                    )

                    FilterChip(
                        selected = filter == ProcessFilter.USER_APPS,
                        onClick = { onFilterChange(ProcessFilter.USER_APPS) },
                        label = { Text("Пользовательские") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // 3. Table Column Headers (Windows Task Manager Style)
        item(key = "table_headers") {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Name Column Header
                    HeaderSortButton(
                        title = "Имя процесса",
                        isSelected = sortField == SortField.NAME,
                        isAscending = sortAscending,
                        onClick = { onSortChange(SortField.NAME) },
                        modifier = Modifier.weight(1f)
                    )

                    // CPU Column Header (Highlighted)
                    HeaderSortButton(
                        title = "ЦП %",
                        isSelected = sortField == SortField.CPU,
                        isAscending = sortAscending,
                        onClick = { onSortChange(SortField.CPU) },
                        modifier = Modifier.width(72.dp)
                    )

                    // RAM Column Header
                    HeaderSortButton(
                        title = "Память",
                        isSelected = sortField == SortField.MEMORY,
                        isAscending = sortAscending,
                        onClick = { onSortChange(SortField.MEMORY) },
                        modifier = Modifier.width(68.dp)
                    )
                }
            }
        }

        // 4. Process List
        if (filteredProcesses.isEmpty()) {
            item(key = "empty_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Процессы не найдены",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Попробуйте изменить параметры поиска или фильтра",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredProcesses,
                key = { it.pid }
            ) { process ->
                ProcessItemCard(
                    process = process,
                    onKillClick = onKillProcess,
                    onDetailsClick = onDetailsClick,
                    onOpenSettingsClick = onOpenSettingsClick,
                    onLaunchClick = onLaunchClick
                )
            }
        }

        item(key = "footer_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TopCpuBanner(
    process: ProcessInfo,
    onKillClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cpuVal = process.cpuPercentage
    val bannerColor = when {
        cpuVal >= 40f -> HeatmapExtreme
        cpuVal >= 20f -> HeatmapHigh
        else -> HeatmapMed
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bannerColor.copy(alpha = 0.14f),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = bannerColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ТОП ПОТРЕБИТЕЛЬ ЦП",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = bannerColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = bannerColor
                ) {
                    Text(
                        text = "${String.format("%.1f", cpuVal)}% ЦП",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = process.appName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "PID: ${process.pid} • ${process.memoryMb.toInt()} МБ ОЗУ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onKillClick,
                        colors = ButtonDefaults.buttonColors(containerColor = bannerColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("top_cpu_kill_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Снять задачу", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSortButton(
    title: String,
    isSelected: Boolean,
    isAscending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp, horizontal = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isSelected) {
            Icon(
                imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(12.dp)
                    .padding(start = 2.dp)
            )
        }
    }
}
