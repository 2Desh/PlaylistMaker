package com.practicum.playlistmaker.presentation.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.Creator

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            sharingInteractor = Creator.provideSharingInteractor(context),
            settingsInteractor = Creator.provideSettingsInteractor(context)
        ) as T
    }
}