package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsInteractor
import com.practicum.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.launch

// Логика создания плейлиста
class PlaylistCreateViewModel(
    private val playlistsInteractor: FavoritePlaylistsInteractor
) : ViewModel() {

    fun createPlaylist(name: String, description: String, coverPath: String?) {
        viewModelScope.launch {
            val playlist = Playlist(
                name = name,
                description = description,
                coverFilePath = coverPath,
                trackIds = emptyList(),
                trackCount = 0
            )
            playlistsInteractor.insertPlaylist(playlist)
        }
    }
}