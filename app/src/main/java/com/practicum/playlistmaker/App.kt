package com.practicum.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson

class App : Application() {

    private val gson by lazy { Gson() }
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)
    }

    val searchHistory by lazy {
        SearchHistory(sharedPreferences, gson)
    }

    // Константы для Shared Preferences
    companion object {
        const val PLAYLIST_MAKER_PREFERENCES = "playlist_maker_preferences"
        const val THEME_SWITCHER_KEY = "theme_switcher_key" // Ключ для сохранения темы
    }

    override fun onCreate() {
        super.onCreate()

        // Загрузка сохраненной темы при старте приложения
        val darkThemeEnabled = sharedPreferences.getBoolean(THEME_SWITCHER_KEY, false)
        applyTheme(darkThemeEnabled)
    }

    // Метод для применения темы
    fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        // Сохранение выбранной темы в SharedPreferences
        sharedPreferences.edit().putBoolean(THEME_SWITCHER_KEY, darkThemeEnabled).apply()
    }
}