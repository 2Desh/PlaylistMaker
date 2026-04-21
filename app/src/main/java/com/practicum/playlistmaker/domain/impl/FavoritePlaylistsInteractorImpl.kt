package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.FavoritePlaylistsInteractor
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsRepository
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

// Реализация логики плейлистов
class FavoritePlaylistsInteractorImpl(
    private val repository: FavoritePlaylistsRepository
) : FavoritePlaylistsInteractor {

    override suspend fun insertPlaylist(playlist: Playlist) {
        repository.insertPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return repository.getPlaylists()
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        return repository.addTrackToPlaylist(track, playlist)
    }
}