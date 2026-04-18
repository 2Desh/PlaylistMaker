package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Обрабатывает результат из репозитория
class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    override fun searchTracks(expression: String): Flow<Pair<List<Track>?, Boolean?>> {
        return repository.searchTracks(expression).map { resource ->
            when {
                resource == null -> Pair(null, true) // Ошибка сети
                resource.isEmpty() -> Pair(emptyList(), null) // Ничего не найдено
                else -> Pair(resource, null) // Успех
            }
        }
    }
}