package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.FavoriteTracksInteractor
import com.practicum.playlistmaker.domain.api.FavoriteTracksRepository
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

// Бизнес логика "Избранных треков"
class FavoriteTracksInteractorImpl(
    private val favoriteTracksRepository: FavoriteTracksRepository
) : FavoriteTracksInteractor {

    override suspend fun insertTrack(track: Track) {
        favoriteTracksRepository.insertTrack(track)
    }

    override suspend fun deleteTrack(trackId: Long) {
        favoriteTracksRepository.deleteTrack(trackId)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return favoriteTracksRepository.getFavoriteTracks()
    }
}