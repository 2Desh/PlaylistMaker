package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

// поиск треков, который реализуется в слое данных
fun interface TracksRepository {
    fun searchTracks(expression: String): List<Track>?
}