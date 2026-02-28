package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.api.TracksConsumer // Добавлен импорт
import java.util.concurrent.Executors

// управляет потоками при поиске треков в сети
class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    // Исправлено: просто consumer: TracksConsumer
    override fun searchTracks(expression: String, consumer: TracksConsumer) {
        executor.execute {
            try {
                val resource = repository.searchTracks(expression)
                if (resource.isNotEmpty()) {
                    consumer.consume(resource, null)
                } else {
                    consumer.consume(null, "Ничего не найдено")
                }
            } catch (e: Exception) {
                consumer.consume(null, e.message)
            }
        }
    }
}