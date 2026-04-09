package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.Response

// общий интерфейс для выполнения сетевых запросов
fun interface NetworkClient {
    fun doRequest(dto: Any): Response
}