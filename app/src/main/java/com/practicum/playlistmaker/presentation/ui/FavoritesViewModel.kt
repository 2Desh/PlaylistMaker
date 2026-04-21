package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.api.FavoriteTracksInteractor
import kotlinx.coroutines.launch

// ViewModel экрана "Избранные треки"
class FavoritesViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<FavoritesState>()
    val stateLiveData: LiveData<FavoritesState> = _stateLiveData

    init {
        fillData()
    }

    private fun fillData() {
        viewModelScope.launch {
            favoriteTracksInteractor.getFavoriteTracks().collect { tracks ->
                if (tracks.isEmpty()) {
                    _stateLiveData.postValue(FavoritesState.Empty)
                } else {
                    _stateLiveData.postValue(FavoritesState.Content(tracks))
                }
            }
        }
    }
}