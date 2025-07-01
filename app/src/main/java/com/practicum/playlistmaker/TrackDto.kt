package com.practicum.playlistmaker

import com.google.gson.annotations.SerializedName

/*
 Класс для объекта трека, как он приходит в ответе от iTunes Search API

     trackName: название
     artistName: исполнитель
     trackTimeMillis: продолжительность в миллисекундах
     artworkUrl100: ссылка на картинку обложки
     всё, кроме продолжительности, может быть null
 */
data class TrackDto(
    @SerializedName("trackName") val trackName: String? = null,
    @SerializedName("artistName") val artistName: String? = null,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long,
    @SerializedName("artworkUrl100") val artworkUrl100: String? = null,
    @SerializedName("trackId") val trackId: Long // id
)
