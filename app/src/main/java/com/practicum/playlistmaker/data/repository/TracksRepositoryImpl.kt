package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.network.NetworkClient
import com.practicum.playlistmaker.data.dto.TrackSearchRequest
import com.practicum.playlistmaker.data.dto.TrackSearchResponse
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.models.Track

// получает данные из сети и превращает trackdto в доменные модели track
class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {

    override fun searchTracks(expression: String): List<Track> {
        val response = networkClient.doRequest(TrackSearchRequest(expression))
        return if (response.resultCode == 200) {
            (response as TrackSearchResponse).results.map {
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
                    it.previewUrl
                )
            }
        } else {
            emptyList()
        }
    }
}