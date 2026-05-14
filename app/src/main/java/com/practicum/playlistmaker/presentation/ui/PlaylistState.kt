package com.practicum.playlistmaker.presentation.ui

import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track

sealed interface PlaylistState {
    object Loading : PlaylistState
    data class Content(
        val playlist: Playlist,
        val tracks: List<Track>,
        val totalDurationMinutes: String
    ) : PlaylistState
}