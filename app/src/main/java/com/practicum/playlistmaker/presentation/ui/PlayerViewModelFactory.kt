package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.Creator

// Фабрика для инстанцирования PlayerViewModel с внедрением зависимости AudioPlayerInteractor
@Suppress("UNCHECKED_CAST")
class PlayerViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayerViewModel(
            audioPlayerInteractor = Creator.provideAudioPlayerInteractor()
        ) as T
    }
}