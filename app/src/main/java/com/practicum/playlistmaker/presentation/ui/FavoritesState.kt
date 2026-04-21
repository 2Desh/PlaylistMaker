package com.practicum.playlistmaker.presentation.ui

import com.practicum.playlistmaker.domain.models.Track

// Описывает возможные состояния экрана "Избранные треки"
sealed interface FavoritesState {
    object Empty : FavoritesState
    data class Content(val tracks: List<Track>) : FavoritesState
}