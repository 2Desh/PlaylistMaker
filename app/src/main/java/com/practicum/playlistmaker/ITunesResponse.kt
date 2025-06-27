package com.practicum.playlistmaker

import com.google.gson.annotations.SerializedName

/*
 Класс для корневого объекта ответа от API
 resultCount - количество найденных результатов
 results - список объектов TrackDto, представляющих найденные треки
 */

data class ITunesResponse(
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("results") val results: List<TrackDto>
)
