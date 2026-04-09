package com.practicum.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

// инициализирует тему при старте и хранит настройки sharedpreferences
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // получаем интерактор через Creator
        val settingsInteractor = Creator.provideSettingsInteractor(this)

        // применяем тему, которую вернул интерактор уже с учетом системы
        applyTheme(settingsInteractor.getThemeSettings())
    }

    fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}