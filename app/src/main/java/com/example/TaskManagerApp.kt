package com.example

import android.app.Application
import com.example.crash.CrashHandler

/**
 * Главный класс приложения.
 * Инициализирует глобальный обработчик сбоев CrashHandler.
 */
class TaskManagerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Устанавливаем глобальный перехватчик сбоев
        CrashHandler.install(this)
    }
}
