package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.SettingsInteractor
import com.practicum.playlistmaker.domain.api.SettingsRepository

// управляет логикой смены темы приложения
class SettingsInteractorImpl(private val repository: SettingsRepository) : SettingsInteractor {
    override fun getThemeSettings(): Boolean {
        return repository.getThemeSettings()
    }

    override fun updateThemeSetting(isDark: Boolean) {
        repository.updateThemeSetting(isDark)
    }
}