package com.practicum.playlistmaker.presentation.ui // или твой пакет, например presentation.models

import com.practicum.playlistmaker.domain.models.Playlist

// Состояния экрана плейлистов
sealed interface PlaylistsState {
    object Empty : PlaylistsState
    data class Content(val playlists: List<Playlist>) : PlaylistsState
}