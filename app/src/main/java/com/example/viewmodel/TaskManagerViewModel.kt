package com.example.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TaskManagerRepository
import com.example.model.AppHistoryUsage
import com.example.model.ProcessImportanceCategory
import com.example.model.ProcessInfo
import com.example.model.SystemPerformanceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TaskManagerTab {
    PROCESSES,
    PERFORMANCE,
    HISTORY
}

enum class ProcessFilter {
    ALL,
    HIGH_CPU,
    USER_APPS
}

enum class SortField {
    CPU,
    MEMORY,
    NAME,
    PID
}

class TaskManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskManagerRepository(application.applicationContext)

    // Active Tab
    private val _selectedTab = MutableStateFlow(TaskManagerTab.PROCESSES)
    val selectedTab: StateFlow<TaskManagerTab> = _selectedTab.asStateFlow()

    // Performance State
    private val _performanceState = MutableStateFlow(SystemPerformanceState())
    val performanceState: StateFlow<SystemPerformanceState> = _performanceState.asStateFlow()

    // Processes List
    private val _allProcesses = MutableStateFlow<List<ProcessInfo>>(emptyList())
    val allProcesses: StateFlow<List<ProcessInfo>> = _allProcesses.asStateFlow()

    // History List
    private val _historyList = MutableStateFlow<List<AppHistoryUsage>>(emptyList())
    val historyList: StateFlow<List<AppHistoryUsage>> = _historyList.asStateFlow()

    // Search and Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(ProcessFilter.ALL)
    val filter: StateFlow<ProcessFilter> = _filter.asStateFlow()

    private val _sortField = MutableStateFlow(SortField.CPU)
    val sortField: StateFlow<SortField> = _sortField.asStateFlow()

    private val _sortAscending = MutableStateFlow(false)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    // Refresh interval: 1000ms, 2000ms, 4000ms, or 0 (paused)
    private val _refreshIntervalMs = MutableStateFlow(2000L)
    val refreshIntervalMs: StateFlow<Long> = _refreshIntervalMs.asStateFlow()

    // Permission status
    private val _isUsageStatsGranted = MutableStateFlow(false)
    val isUsageStatsGranted: StateFlow<Boolean> = _isUsageStatsGranted.asStateFlow()

    // Selected process for dialog details
    private val _selectedProcessForDetails = MutableStateFlow<ProcessInfo?>(null)
    val selectedProcessForDetails: StateFlow<ProcessInfo?> = _selectedProcessForDetails.asStateFlow()

    // User notification events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private var monitorJob: Job? = null

    init {
        checkPermissions()
        startMonitoring()
    }

    fun selectTab(tab: TaskManagerTab) {
        _selectedTab.value = tab
        if (tab == TaskManagerTab.HISTORY) {
            refreshHistory()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: ProcessFilter) {
        _filter.value = filter
    }

    fun setSorting(field: SortField) {
        if (_sortField.value == field) {
            _sortAscending.value = !_sortAscending.value
        } else {
            _sortField.value = field
            // Default CPU and Memory to descending, Name and PID to ascending
            _sortAscending.value = (field == SortField.NAME || field == SortField.PID)
        }
    }

    fun setRefreshInterval(ms: Long) {
        _refreshIntervalMs.value = ms
        startMonitoring()
    }

    fun selectProcessForDetails(process: ProcessInfo?) {
        _selectedProcessForDetails.value = process
    }

    fun refreshNow() {
        viewModelScope.launch {
            fetchData()
            if (_selectedTab.value == TaskManagerTab.HISTORY) {
                refreshHistory()
            }
        }
    }

    fun checkPermissions() {
        _isUsageStatsGranted.value = repository.isUsageStatsPermissionGranted()
    }

    fun refreshHistory() {
        viewModelScope.launch {
            checkPermissions()
            _historyList.value = repository.getAppHistory()
        }
    }

    fun killProcess(process: ProcessInfo) {
        viewModelScope.launch {
            val killed = repository.killBackgroundProcess(process.packageName)
            if (killed) {
                _userMessage.emit("Фоновые процессы «${process.appName}» завершены")
            } else {
                _userMessage.emit("Не удалось остановить «${process.appName}» напрямую")
            }
            delay(300)
            fetchData()
        }
    }

    fun getOpenAppSettingsIntent(packageName: String): Intent {
        return repository.createOpenAppSettingsIntent(packageName)
    }

    fun getUsageAccessSettingsIntent(): Intent {
        return repository.createUsageAccessSettingsIntent()
    }

    fun getLaunchAppIntent(packageName: String): Intent? {
        return repository.launchAppIntent(packageName)
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        val interval = _refreshIntervalMs.value
        if (interval <= 0L) return

        monitorJob = viewModelScope.launch {
            while (isActive) {
                fetchData()
                delay(interval)
            }
        }
    }

    private suspend fun fetchData() {
        checkPermissions()
        val (performance, processes) = repository.getPerformanceAndProcesses()
        _performanceState.value = performance
        _allProcesses.value = processes
    }

    override fun onCleared() {
        super.onCleared()
        monitorJob?.cancel()
    }
}
