package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsInteractor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Логика экрана плейлиста. Готовит данные для фрагмента, пересчитывает минуты, вызывает удаление из БД
class PlaylistViewModel(
    private val playlistId: Long,
    private val interactor: FavoritePlaylistsInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<PlaylistState>(PlaylistState.Loading)
    val stateLiveData: LiveData<PlaylistState> = _stateLiveData

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            interactor.getPlaylistById(playlistId).collect { playlist ->
                if (playlist != null) {
                    interactor.getTracksForPlaylist(playlist.trackIds).collect { tracks ->
                        val durationSum = tracks.sumOf { it.trackTime }
                        val minutes = SimpleDateFormat("mm", Locale.getDefault()).format(durationSum)

                        _stateLiveData.postValue(PlaylistState.Content(playlist, tracks, minutes))
                    }
                }
            }
        }
    }

    fun deleteTrack(trackId: Long) {
        viewModelScope.launch {
            interactor.deleteTrackFromPlaylist(trackId, playlistId)
            loadData()
        }
    }
    fun deletePlaylist() {
        viewModelScope.launch {
            interactor.deletePlaylist(playlistId)
        }
    }
}