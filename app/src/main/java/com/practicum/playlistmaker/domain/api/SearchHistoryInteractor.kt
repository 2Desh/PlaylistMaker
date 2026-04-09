package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

// логика работы с историей
interface SearchHistoryInteractor {
    fun getHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearHistory()
}