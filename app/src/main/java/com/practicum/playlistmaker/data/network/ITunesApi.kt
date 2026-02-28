package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.dto.TrackSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// интерфейс с описанием конечных точек retrofit
@Suppress("kotlin:S6517")
interface ITunesApi {
    @GET("/search?entity=song")
    fun search(@Query("term") text: String): Call<TrackSearchResponse>
}