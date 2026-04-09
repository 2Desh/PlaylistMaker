package com.practicum.playlistmaker.presentation.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.Creator

// Фабрика для инстанцирования SearchViewModel с внедрением зависимостей TracksInteractor и SearchHistoryInteractor
class SearchViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(
            tracksInteractor = Creator.provideTracksInteractor(),
            searchHistoryInteractor = Creator.provideSearchHistoryInteractor(context.applicationContext)
        ) as T
    }
}