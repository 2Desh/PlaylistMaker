package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

// запуск поиска треков через доменный слой
fun interface TracksInteractor {
    fun searchTracks(expression: String): Flow<Pair<List<Track>?, Boolean?>>
}