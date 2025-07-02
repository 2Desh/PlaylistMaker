package com.practicum.playlistmaker

data class Track(
    val trackName: String, // Название
    val artistName: String, // Исполнитель
    val trackTime: String, // Продолжительность
    val artworkUrl100: String, // Обложка
    val trackId: Long // id
)