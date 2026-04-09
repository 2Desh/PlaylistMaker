package com.practicum.playlistmaker.domain.api

// получение и изменение настроек темы
interface SettingsInteractor {
    fun getThemeSettings(): Boolean
    fun updateThemeSetting(isDark: Boolean)
}