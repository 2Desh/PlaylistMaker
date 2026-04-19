package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.presentation.ui.FavoritesViewModel
import com.practicum.playlistmaker.presentation.ui.PlayerViewModel
import com.practicum.playlistmaker.presentation.ui.PlaylistsViewModel
import com.practicum.playlistmaker.presentation.ui.SearchViewModel
import com.practicum.playlistmaker.presentation.ui.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Описание того, как Koin должен создавать ViewModel
val viewModelModule = module {

    viewModel {
        PlayerViewModel(get(), get())
    }

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        SettingsViewModel(get(), get())
    }

    viewModel {
        FavoritesViewModel(get())
    }

    viewModel {
        PlaylistsViewModel()
    }
}