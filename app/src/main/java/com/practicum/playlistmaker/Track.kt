package com.practicum.playlistmaker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val trackName: String,          // Название
    val artistName: String,         // Исполнитель
    val trackTime: Long,            // Продолжительность в милисек.
    val artworkUrl100: String,      // Обложка
    val trackId: Long,               // ID трека
    val collectionName: String?,    // Название альбома
    val releaseDate: String?,       // Год релиза
    val primaryGenreName: String?,  // Жанр
    val country: String?            // Страна
) : Parcelable {
    // Функция для получения ссылки на обложку размером 512x512
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
}