package com.practicum.playlistmaker.domain.api

// запуск поиска треков через доменный слой
fun interface TracksInteractor {
    fun searchTracks(expression: String, consumer: TracksConsumer)
}