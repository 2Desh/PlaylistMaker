package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.data.dto.TrackSearchRequest
import com.practicum.playlistmaker.data.dto.TrackSearchResponse
import com.practicum.playlistmaker.data.network.NetworkClient
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Реализация репозитория для поиска треков.
class TracksRepositoryImpl(
    private val networkClient: NetworkClient,
    private val appDatabase: AppDatabase
) : TracksRepository {

    override fun searchTracks(expression: String): Flow<List<Track>?> = flow {
        val response = networkClient.doRequest(TrackSearchRequest(expression))

        when (response.resultCode) {
            200 -> {
                val favoriteTracksIds = appDatabase.trackDao().getFavoriteTrackIds()
                val data = (response as TrackSearchResponse).results.map {
                    Track(
                        it.trackName ?: "",
                        it.artistName ?: "",
                        it.trackTimeMillis,
                        it.artworkUrl100 ?: "",
                        it.trackId,
                        it.collectionName,
                        it.releaseDate,
                        it.primaryGenreName,
                        it.country,
                        it.previewUrl,
                        favoriteTracksIds.contains(it.trackId)
                    )
                }
                emit(data) // отправляем данные в поток
            }
            else -> {
                emit(null) // отправляем ошибку сети в поток
            }
        }
    }
}