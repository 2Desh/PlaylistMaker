package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.domain.models.Track

// проверяет лимит в !10! треков и удаляет дубликаты в истории
class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }

    override fun getHistory(): List<Track> {
        return repository.getHistory()
    }

    override fun addTrackToHistory(track: Track) {
        val history = repository.getHistory().toMutableList()
        val existingTrackIndex = history.indexOfFirst { it.trackId == track.trackId }

        if (existingTrackIndex != -1) {
            history.removeAt(existingTrackIndex)
        }

        history.add(0, track)

        if (history.size > MAX_HISTORY_SIZE) {
            history.removeLast()
        }

        repository.saveHistory(history)
    }

    override fun clearHistory() {
        repository.clearHistory()
    }
}