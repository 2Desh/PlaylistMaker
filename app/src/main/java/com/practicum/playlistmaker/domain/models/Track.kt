package com.practicum.playlistmaker.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// база
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
    val country: String?,            // Страна
    val previewUrl: String? = null,   // Превью
    var isFavorite: Boolean = false  // Лайк
) : Parcelable {
    // Функция для получения ссылки на обложку
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
}