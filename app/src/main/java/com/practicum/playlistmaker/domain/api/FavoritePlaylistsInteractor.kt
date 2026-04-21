package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.flow.Flow
import com.practicum.playlistmaker.domain.models.Track

// Интерфейс логики плейлистов
interface FavoritePlaylistsInteractor {
    suspend fun insertPlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean
}