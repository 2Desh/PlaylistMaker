package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsInteractor
import com.practicum.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.launch

// Логика создания и редактирования плейлиста
class PlaylistCreateViewModel(
    private val playlistsInteractor: FavoritePlaylistsInteractor
) : ViewModel() {

    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean> = _isSaved

    private val _playlist = MutableLiveData<Playlist>()
    val playlist: LiveData<Playlist> = _playlist

    fun loadPlaylist(id: Long) {
        viewModelScope.launch {
            playlistsInteractor.getPlaylistById(id).collect { foundPlaylist ->
                foundPlaylist?.let {
                    _playlist.postValue(it)
                }
            }
        }
    }

    // Метод для создания нового плейлиста
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
            _isSaved.postValue(true)
        }
    }

    // Метод для обновления существующего плейлиста
    fun updatePlaylist(
        id: Long,
        name: String,
        description: String,
        coverFilePath: String?,
        trackIds: List<Long>,
        trackCount: Int
    ) {
        viewModelScope.launch {
            val updatedPlaylist = Playlist(
                id = id,
                name = name,
                description = description,
                coverFilePath = coverFilePath,
                trackIds = trackIds,
                trackCount = trackCount
            )
            // Обновляем плейлист в БД
            playlistsInteractor.updatePlaylist(updatedPlaylist)

            // Сообщаем фрагменту, что можно закрывать экран
            _isSaved.postValue(true)
        }
    }
}