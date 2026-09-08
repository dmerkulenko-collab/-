package com.example.crash

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Process
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

/**
 * Глобальный обработчик неперехваченных исключений.
 * Перехватывает сбои, формирует подробный технический отчет для разработчика / ИИ,
 * автоматически копирует его в системный буфер обмена и запускает экран CrashReportActivity.
 */
class CrashHandler private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val report = buildCrashReport(thread, throwable)
            Log.e(TAG, "Uncaught exception intercepted:\n$report", throwable)

            // 1. Сохраняем в SharedPreferences
            saveCrashReport(context, report)

            // 2. Копируем в буфер обмена
            copyToClipboard(context, report)

            // 3. Запускаем окно отчёта об ошибке CrashReportActivity
            val intent = Intent(context, CrashReportActivity::class.java).apply {
                putExtra(EXTRA_CRASH_REPORT, report)
                putExtra(EXTRA_EXCEPTION_NAME, throwable.javaClass.simpleName)
                putExtra(EXTRA_EXCEPTION_MESSAGE, throwable.localizedMessage ?: "Unknown error")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)

            // Завершаем аварийный процесс
            Process.killProcess(Process.myPid())
            exitProcess(10)
        } catch (e: Throwable) {
            Log.e(TAG, "Error in CrashHandler uncaughtException handling", e)
            defaultHandler?.uncaughtException(thread, throwable) ?: run {
                Process.killProcess(Process.myPid())
                exitProcess(1)
            }
        }
    }

    private fun buildCrashReport(thread: Thread, throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        val stackTraceString = sw.toString()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val crashTime = dateFormat.format(Date())

        val packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
        val appVersionName = packageInfo?.versionName ?: "1.0"
        val appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode ?: 1L
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode?.toLong() ?: 1L
        }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val availMemMb = memoryInfo.availMem / (1024 * 1024)
        val totalMemMb = memoryInfo.totalMem / (1024 * 1024)

        return buildString {
            appendLine("=== ДИСПЕТЧЕР ЗАДАЧ: ОТЧЕТ ОБ ОШИБКЕ ===")
            appendLine("Время сбоя: $crashTime")
            appendLine("Приложение: ${context.packageName}")
            appendLine("Версия: $appVersionName (код сборки: $appVersionCode)")
            appendLine()
            appendLine("--- СИСТЕМА И УСТРОЙСТВО ---")
            appendLine("Устройство: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
            appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Архитектура CPU: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
            appendLine("ОЗУ устройства: $availMemMb МБ свободно из $totalMemMb МБ (lowMemory=${memoryInfo.lowMemory})")
            appendLine()
            appendLine("--- ИНФОРМАЦИЯ ОБ ИСКЛЮЧЕНИИ ---")
            appendLine("Поток выполнения: ${thread.name} (id: ${thread.id})")
            appendLine("Класс ошибки: ${throwable.javaClass.name}")
            appendLine("Сообщение: ${throwable.localizedMessage ?: "(нет описания)"}")
            appendLine()
            appendLine("--- ПОЛНЫЙ СТЕК ВЫЗОВОВ (STACK TRACE) ---")
            appendLine(stackTraceString.trim())
            appendLine()
            val cause = throwable.cause
            if (cause != null) {
                appendLine("--- ПЕРВОПРИЧИНА (CAUSED BY) ---")
                val causeSw = StringWriter()
                cause.printStackTrace(PrintWriter(causeSw))
                appendLine(causeSw.toString().trim())
                appendLine()
            }
            appendLine("==========================================")
        }
    }

    companion object {
        private const val TAG = "CrashHandler"
        private const val PREFS_NAME = "crash_reporter_prefs"
        private const val KEY_LAST_REPORT = "last_crash_report"
        private const val KEY_CRASH_TIMESTAMP = "last_crash_timestamp"
        private const val KEY_REPORT_PENDING_REVIEW = "report_pending_review"

        const val EXTRA_CRASH_REPORT = "extra_crash_report"
        const val EXTRA_EXCEPTION_NAME = "extra_exception_name"
        const val EXTRA_EXCEPTION_MESSAGE = "extra_exception_message"

        fun install(context: Context) {
            val handler = CrashHandler(context.applicationContext)
            Thread.setDefaultUncaughtExceptionHandler(handler)
            Log.i(TAG, "CrashHandler installed successfully")
        }

        fun copyToClipboard(context: Context, text: String): Boolean {
            return try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("Crash Report", text)
                clipboard?.setPrimaryClip(clip)
                true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to copy crash report to clipboard", e)
                false
            }
        }

        fun saveCrashReport(context: Context, report: String) {
            try {
                val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putString(KEY_LAST_REPORT, report)
                    .putLong(KEY_CRASH_TIMESTAMP, System.currentTimeMillis())
                    .putBoolean(KEY_REPORT_PENDING_REVIEW, true)
                    .apply()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save crash report to prefs", e)
            }
        }

        fun getLastCrashReport(context: Context): String? {
            return try {
                val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.getString(KEY_LAST_REPORT, null)
            } catch (_: Exception) {
                null
            }
        }

        fun isCrashPendingReview(context: Context): Boolean {
            return try {
                val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.getBoolean(KEY_REPORT_PENDING_REVIEW, false)
            } catch (_: Exception) {
                false
            }
        }

        fun markCrashReviewed(context: Context) {
            try {
                val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean(KEY_REPORT_PENDING_REVIEW, false)
                    .apply()
            } catch (_: Exception) {}
        }

        fun clearCrashReport(context: Context) {
            try {
                val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
            } catch (_: Exception) {}
        }

        /**
         * Формирует диагностический отчёт об устройстве, даже если сбоев не зафиксировано
         */
        fun getOrGenerateReport(context: Context): String {
            val lastCrash = getLastCrashReport(context)
            if (!lastCrash.isNullOrBlank()) {
                return lastCrash
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val nowStr = dateFormat.format(Date())

            val packageInfo = try {
                context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (_: Exception) {
                null
            }
            val appVersionName = packageInfo?.versionName ?: "1.0"
            val appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo?.longVersionCode ?: 1L
            } else {
                @Suppress("DEPRECATION")
                packageInfo?.versionCode?.toLong() ?: 1L
            }

            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager?.getMemoryInfo(memoryInfo)
            val availMemMb = memoryInfo.availMem / (1024 * 1024)
            val totalMemMb = memoryInfo.totalMem / (1024 * 1024)

            return buildString {
                appendLine("=== ДИСПЕТЧЕР ЗАДАЧ: ДИАГНОСТИЧЕСКИЙ ОТЧЕТ ===")
                appendLine("Время формирования: $nowStr")
                appendLine("Статус: Сбоев не зафиксировано (система стабильна)")
                appendLine("Приложение: ${context.packageName}")
                appendLine("Версия: $appVersionName (код сборки: $appVersionCode)")
                appendLine()
                appendLine("--- СИСТЕМА И УСТРОЙСТВО ---")
                appendLine("Устройство: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
                appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine("Архитектура CPU: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
                appendLine("ОЗУ: $availMemMb МБ свободно из $totalMemMb МБ (lowMemory=${memoryInfo.lowMemory})")
                appendLine("==========================================")
            }
        }

        /**
         * Имитация аварийного сбоя для тестирования перехватчика и копирования в буфер обмена
         */
        fun triggerTestCrash() {
            throw RuntimeException("Тестовый сбой: Проверка работы CrashHandler и перехвата стек-трейса в буфер обмена")
        }
    }
}
