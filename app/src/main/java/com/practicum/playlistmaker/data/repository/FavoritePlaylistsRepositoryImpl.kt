package com.practicum.playlistmaker.data.repository

import com.google.gson.Gson
import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.data.db.PlaylistDbConvertor
import com.practicum.playlistmaker.data.db.entity.PlaylistEntity
import com.practicum.playlistmaker.data.db.entity.PlaylistTrackEntity
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsRepository
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Репозиторий данных
class FavoritePlaylistsRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val playlistDbConvertor: PlaylistDbConvertor,
    private val gson: Gson
) : FavoritePlaylistsRepository {

    override suspend fun insertPlaylist(playlist: Playlist) {
        appDatabase.playlistDao().insertPlaylist(playlistDbConvertor.map(playlist))
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        appDatabase.playlistDao().updatePlaylist(playlistDbConvertor.map(playlist))
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return appDatabase.playlistDao().getPlaylists().map { playlists ->
            playlists.map { playlistDbConvertor.map(it) }
        }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        // Проверяем, есть ли уже такой трек в списке этого плейлиста
        if (playlist.trackIds.contains(track.trackId)) {
            return false
        }

        // Добавляем ID трека в список плейлиста
        val updatedTrackIds = ArrayList(playlist.trackIds)
        updatedTrackIds.add(0, track.trackId)

        // Сохраняем сам трек в отдельную таблицу всех треков из плейлистов
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
        appDatabase.playlistTrackDao().insertTrack(trackEntity)

        // Обновляем информацию о плейлисте в БД
        val updatedPlaylistEntity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverFilePath = playlist.coverFilePath,
            trackIds = gson.toJson(updatedTrackIds),
            trackCount = playlist.trackCount + 1
        )
        appDatabase.playlistDao().updatePlaylist(updatedPlaylistEntity)

        return true
    }
}