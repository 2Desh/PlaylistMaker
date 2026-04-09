package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.api.SettingsInteractor
import com.practicum.playlistmaker.domain.api.SharingInteractor

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    init {
        _themeState.postValue(settingsInteractor.getThemeSettings())
    }

    // Метод для обновления темы
    fun updateTheme(isDark: Boolean) {
        settingsInteractor.updateThemeSetting(isDark) // сохраняем в память
        _themeState.postValue(isDark)                 // обновляем LiveData
    }

    // Методы для кнопок
    fun shareApp() {
        sharingInteractor.shareApp()
    }

    fun openSupport() {
        sharingInteractor.openSupport()
    }

    fun openTerms() {
        sharingInteractor.openTerms()
    }
}