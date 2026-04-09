package com.practicum.playlistmaker.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.practicum.playlistmaker.domain.api.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val context: Context
) : SettingsRepository {

    override fun getThemeSettings(): Boolean {
        return if (!sharedPrefs.contains(THEME_SWITCHER_KEY)) {
            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            uiMode == Configuration.UI_MODE_NIGHT_YES
        } else {
            sharedPrefs.getBoolean(THEME_SWITCHER_KEY, false)
        }
    }

    override fun updateThemeSetting(isDark: Boolean) {
        sharedPrefs.edit().putBoolean(THEME_SWITCHER_KEY, isDark).apply()
    }

    companion object {
        const val THEME_SWITCHER_KEY = "theme_switcher_key"
    }
}