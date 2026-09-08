package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.crash.CrashHandler
import com.example.crash.CrashReportActivity
import com.example.ui.components.HistoryView
import com.example.ui.components.PerformanceView
import com.example.ui.components.ProcessDetailsDialog
import com.example.ui.components.ProcessesView
import com.example.ui.components.TaskManagerTabs
import com.example.ui.components.WindowsTopBar
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TaskManagerTab
import com.example.viewmodel.TaskManagerViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: TaskManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                TaskManagerApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
        viewModel.refreshNow()
    }
}

@Composable
fun TaskManagerApp(
    viewModel: TaskManagerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // State collections
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val performance by viewModel.performanceState.collectAsStateWithLifecycle()
    val allProcesses by viewModel.allProcesses.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val sortField by viewModel.sortField.collectAsStateWithLifecycle()
    val sortAscending by viewModel.sortAscending.collectAsStateWithLifecycle()
    val refreshIntervalMs by viewModel.refreshIntervalMs.collectAsStateWithLifecycle()
    val isUsageStatsGranted by viewModel.isUsageStatsGranted.collectAsStateWithLifecycle()
    val selectedProcessForDetails by viewModel.selectedProcessForDetails.collectAsStateWithLifecycle()

    // Handle user snackbars / toasts and check for previous crash report
    LaunchedEffect(Unit) {
        if (CrashHandler.isCrashPendingReview(context)) {
            val report = CrashHandler.getLastCrashReport(context) ?: "Отчёт пуст"
            CrashHandler.copyToClipboard(context, report)
            val result = snackbarHostState.showSnackbar(
                message = "Обнаружен отчёт о сбое! Текст скопирован в буфер обмена.",
                actionLabel = "Открыть",
                duration = SnackbarDuration.Long
            )
            CrashHandler.markCrashReviewed(context)
            if (result == SnackbarResult.ActionPerformed) {
                val intent = Intent(context, CrashReportActivity::class.java).apply {
                    putExtra(CrashHandler.EXTRA_CRASH_REPORT, report)
                    putExtra(CrashHandler.EXTRA_EXCEPTION_NAME, "Предыдущий сбой")
                    putExtra(CrashHandler.EXTRA_EXCEPTION_MESSAGE, "Аварийное завершение работы")
                }
                context.startActivity(intent)
            }
        }
        viewModel.userMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            WindowsTopBar(
                performance = performance,
                refreshIntervalMs = refreshIntervalMs,
                onRefreshIntervalChange = { viewModel.setRefreshInterval(it) },
                onRefreshClick = { viewModel.refreshNow() },
                onViewCrashReport = {
                    val report = CrashHandler.getOrGenerateReport(context)
                    val intent = Intent(context, CrashReportActivity::class.java).apply {
                        putExtra(CrashHandler.EXTRA_CRASH_REPORT, report)
                        putExtra(CrashHandler.EXTRA_EXCEPTION_NAME, "Диагностика системы")
                        putExtra(CrashHandler.EXTRA_EXCEPTION_MESSAGE, "Журнал сбоев и диагностика оборудования")
                    }
                    context.startActivity(intent)
                },
                onTriggerTestCrash = {
                    Toast.makeText(context, "Имитация сбоя для проверки перехватчика...", Toast.LENGTH_SHORT).show()
                    CrashHandler.triggerTestCrash()
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Task Manager Tab Strip (Processes, Performance, History)
                TaskManagerTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )

                // Main Content View
                Crossfade(
                    targetState = selectedTab,
                    label = "tab_crossfade",
                    modifier = Modifier.weight(1f)
                ) { tab ->
                    when (tab) {
                        TaskManagerTab.PROCESSES -> {
                            ProcessesView(
                                processes = allProcesses,
                                performance = performance,
                                isUsageStatsGranted = isUsageStatsGranted,
                                onRequestPermissionClick = {
                                    val intent = viewModel.getUsageAccessSettingsIntent()
                                    context.startActivity(intent)
                                },
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                filter = filter,
                                onFilterChange = { viewModel.setFilter(it) },
                                sortField = sortField,
                                sortAscending = sortAscending,
                                onSortChange = { viewModel.setSorting(it) },
                                onKillProcess = { viewModel.killProcess(it) },
                                onDetailsClick = { viewModel.selectProcessForDetails(it) },
                                onOpenSettingsClick = { proc ->
                                    val intent = viewModel.getOpenAppSettingsIntent(proc.packageName)
                                    context.startActivity(intent)
                                },
                                onLaunchClick = { proc ->
                                    val launchIntent = viewModel.getLaunchAppIntent(proc.packageName)
                                    if (launchIntent != null) {
                                        context.startActivity(launchIntent)
                                    } else {
                                        Toast.makeText(context, "Приложение не имеет экрана запуска", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        TaskManagerTab.PERFORMANCE -> {
                            PerformanceView(
                                performance = performance
                            )
                        }

                        TaskManagerTab.HISTORY -> {
                            HistoryView(
                                historyList = historyList,
                                isPermissionGranted = isUsageStatsGranted,
                                onRequestPermissionClick = {
                                    val intent = viewModel.getUsageAccessSettingsIntent()
                                    context.startActivity(intent)
                                },
                                onRefreshClick = { viewModel.refreshHistory() }
                            )
                        }
                    }
                }
            }
        }

        // Detailed Process Inspection Dialog
        selectedProcessForDetails?.let { process ->
            ProcessDetailsDialog(
                process = process,
                onDismiss = { viewModel.selectProcessForDetails(null) },
                onKill = { viewModel.killProcess(it) },
                onOpenSettings = { proc ->
                    val intent = viewModel.getOpenAppSettingsIntent(proc.packageName)
                    context.startActivity(intent)
                },
                onLaunch = { proc ->
                    val launchIntent = viewModel.getLaunchAppIntent(proc.packageName)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    } else {
                        Toast.makeText(context, "Приложение не имеет экрана запуска", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}
