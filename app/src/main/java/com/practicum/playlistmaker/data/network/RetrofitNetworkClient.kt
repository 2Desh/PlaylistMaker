package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.Response
import com.practicum.playlistmaker.data.dto.TrackSearchRequest
import kotlinx.coroutines.CancellationException

// Реализация интерфейса сетевого взаимодействия. Выполняет HTTP-запросы к API и возвращает статус ответа.
class RetrofitNetworkClient(private val itunesService: ITunesApi) : NetworkClient {

    override suspend fun doRequest(dto: Any): Response {
        if (dto !is TrackSearchRequest) {
            return Response().apply { resultCode = 400 }
        }

        return try {
            val resp = itunesService.search(dto.expression)
            resp.apply { resultCode = 200 }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            Response().apply { resultCode = -1 }
        }
    }
}