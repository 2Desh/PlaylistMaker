package com.practicum.playlistmaker.data.db

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.db.entity.PlaylistEntity
import com.practicum.playlistmaker.domain.models.Playlist

// Маппинг данных БД
class PlaylistDbConvertor(private val gson: Gson) {

    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverFilePath = playlist.coverFilePath,
            trackIds = gson.toJson(playlist.trackIds),
            trackCount = playlist.trackCount
        )
    }

    fun map(entity: PlaylistEntity): Playlist {
        val type = object : TypeToken<List<Long>>() {}.type
        val trackIdsList: List<Long> = gson.fromJson(entity.trackIds, type) ?: emptyList()

        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            coverFilePath = entity.coverFilePath,
            trackIds = trackIdsList,
            trackCount = entity.trackCount
        )
    }
}