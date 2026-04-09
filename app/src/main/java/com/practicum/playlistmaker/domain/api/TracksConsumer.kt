package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

//интерфейс-коллбэк для возврата результатов поиска в активити
fun interface TracksConsumer {
    fun consume(foundTracks: List<Track>?, errorMessage: String?)
}