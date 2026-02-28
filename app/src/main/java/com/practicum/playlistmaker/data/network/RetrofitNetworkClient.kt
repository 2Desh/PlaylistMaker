package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.Response
import com.practicum.playlistmaker.data.dto.TrackSearchRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// реализация клиента, которая физически идет в интернет через retrofit
class RetrofitNetworkClient : NetworkClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl(itunes_base_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesService = retrofit.create(ITunesApi::class.java)

    override fun doRequest(dto: Any): Response {
        if (dto !is TrackSearchRequest) {
            return Response().apply { resultCode = 400 }
        }

        return try {
            val resp = itunesService.search(dto.expression).execute()
            val body = resp.body()
            body?.apply { resultCode = resp.code() } ?: Response().apply { resultCode = resp.code() }
        } catch (e: Exception) {
            // если таймаут или нет сети, возвращаем код ошибки
            Response().apply { resultCode = -1 }
        }
    }

    companion object {
        private const val itunes_base_url = "https://itunes.apple.com"
    }
}