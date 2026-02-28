package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

// функции работы с хранилищем истории
interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun saveHistory(history: List<Track>)
    fun clearHistory()
}