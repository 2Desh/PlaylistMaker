package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.Response
import com.practicum.playlistmaker.data.dto.TrackSearchRequest

// Реализация интерфейса сетевого взаимодействия. Выполняет HTTP-запросы к API и возвращает статус ответа.
class RetrofitNetworkClient(private val itunesService: ITunesApi) : NetworkClient {

    override fun doRequest(dto: Any): Response {
        if (dto !is TrackSearchRequest) {
            return Response().apply { resultCode = 400 }
        }

        return try {
            val resp = itunesService.search(dto.expression).execute()
            val body = resp.body()
            body?.apply { resultCode = resp.code() } ?: Response().apply { resultCode = resp.code() }
        } catch (_: Exception) {
            Response().apply { resultCode = -1 }
        }
    }
}