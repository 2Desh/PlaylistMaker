package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsInteractor
import kotlinx.coroutines.launch

// Логика списка плейлистов
class PlaylistsViewModel(
    private val interactor: FavoritePlaylistsInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<PlaylistsState>()
    val stateLiveData: LiveData<PlaylistsState> = _stateLiveData

    init {
        fillData()
    }

    fun fillData() {
        viewModelScope.launch {
            interactor.getPlaylists().collect { playlists ->
                if (playlists.isEmpty()) {
                    _stateLiveData.postValue(PlaylistsState.Empty)
                } else {
                    _stateLiveData.postValue(PlaylistsState.Content(playlists))
                }
            }
        }
    }
}