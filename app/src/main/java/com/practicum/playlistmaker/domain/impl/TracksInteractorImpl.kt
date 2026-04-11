package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.api.TracksConsumer
import java.util.concurrent.Executors

// Управляет потоками при поиске треков в сети
class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    private val executor = Executors.newCachedThreadPool()

    override fun searchTracks(expression: String, consumer: TracksConsumer) {
        executor.execute {
            try {
                val resource = repository.searchTracks(expression)

                when {
                    resource == null -> {
                        // Нет связи
                        consumer.consume(null, "Ошибка сети")
                    }
                    resource.isEmpty() -> {
                        // Нет таких данных
                        consumer.consume(null, "Ничего не найдено")
                    }
                    else -> {
                        // Успех
                        consumer.consume(resource, null)
                    }
                }
            } catch (e: Exception) {
                consumer.consume(null, e.message)
            }
        }
    }
}