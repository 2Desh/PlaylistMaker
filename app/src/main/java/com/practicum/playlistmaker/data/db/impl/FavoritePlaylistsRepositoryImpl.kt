package com.practicum.playlistmaker.data.db.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.db.dao.PlaylistDao
import com.practicum.playlistmaker.data.db.dao.PlaylistTrackDao
import com.practicum.playlistmaker.data.db.entity.PlaylistEntity
import com.practicum.playlistmaker.data.db.entity.PlaylistTrackEntity
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsRepository
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Реализация хранилища плейлистов
class FavoritePlaylistsRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao,
    private val gson: Gson
) : FavoritePlaylistsRepository {

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylists().map { entityList ->
            entityList.map { mapToDomain(it) }
        }
    }

    override suspend fun insertPlaylist(playlist: Playlist) {
        val entity = PlaylistEntity(
            name = playlist.name,
            description = playlist.description,
            coverFilePath = playlist.coverFilePath,
            trackIds = gson.toJson(playlist.trackIds),
            trackCount = playlist.trackCount
        )
        playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverFilePath = playlist.coverFilePath,
            trackIds = gson.toJson(playlist.trackIds),
            trackCount = playlist.trackCount
        )
        playlistDao.updatePlaylist(entity)
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        val currentTrackIds = ArrayList(playlist.trackIds)

        if (currentTrackIds.contains(track.trackId)) {
            return false
        }

        // Сохраняем сам трек в таблицу всех треков плейлистов
        val trackEntity = PlaylistTrackEntity(
            trackId = track.trackId.toString(),
            trackName = track.trackName,
            artistName = track.artistName,
            trackTimeMillis = track.trackTime,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl,
            insertTime = System.currentTimeMillis()
        )
        playlistTrackDao.insertTrack(trackEntity)

        // Добавляем ID нового трека в начало списка
        currentTrackIds.add(0, track.trackId)
        val newTrackIdsJson = gson.toJson(currentTrackIds)

        // Обновляем плейлист в базе
        val updatedPlaylistEntity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverFilePath = playlist.coverFilePath,
            trackIds = newTrackIdsJson,
            trackCount = playlist.trackCount + 1
        )
        playlistDao.updatePlaylist(updatedPlaylistEntity)

        return true
    }

    // Вспомогательный метод для перевода из Entity БД в доменную модель Playlist
    private fun mapToDomain(entity: PlaylistEntity): Playlist {
        val type = object : TypeToken<List<Long>>() {}.type
        val trackIdsList: List<Long> = if (entity.trackIds.isNotEmpty()) {
            gson.fromJson(entity.trackIds, type) ?: emptyList()
        } else {
            emptyList()
        }

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