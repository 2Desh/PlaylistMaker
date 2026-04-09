package com.practicum.playlistmaker.domain.api

// методы доступа к настройкам темы
interface SettingsRepository {
    fun getThemeSettings(): Boolean
    fun updateThemeSetting(isDark: Boolean)
}