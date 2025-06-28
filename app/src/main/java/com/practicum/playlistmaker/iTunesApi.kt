package com.practicum.playlistmaker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/*
    Интерфейс для взаимодействия с iTunes Search API
    Определяется GET запрос для поиска треков и его путь.

    Call<ITunesResponse> вернет объект типа ITunesResponse
*/

interface iTunesApi {
    @GET("/search?entity=song")
    fun search(@Query("term") text: String): Call<ITunesResponse>
}
