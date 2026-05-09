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
import kotlinx.coroutines.flow.flow
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
        if (playlist.trackIds.contains(track.trackId)) return false

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

    override fun getPlaylistById(id: Long): Flow<Playlist?> {
        return appDatabase.playlistDao().getPlaylistById(id).map { entity ->
            entity?.let { playlistDbConvertor.map(it) }
        }
    }

    override fun getTracksForPlaylist(trackIds: List<Long>): Flow<List<Track>> = flow {
        // Получаем все сохраненные треки
        val allTracksEntity = appDatabase.playlistTrackDao().getAllTracks()
        // Делаем из них словарь для быстрого поиска
        val tracksMap = allTracksEntity.associateBy { it.trackId.toLong() }

        // Мапим по списку ID
        val result = trackIds.mapNotNull { id -> tracksMap[id] }.map { entity ->
            Track(
                trackId = entity.trackId.toLong(),
                trackName = entity.trackName ?: "",
                artistName = entity.artistName ?: "",
                trackTime = entity.trackTimeMillis ?: 0L,
                artworkUrl100 = entity.artworkUrl100 ?: "",
                collectionName = entity.collectionName ?: "",
                releaseDate = entity.releaseDate ?: "",
                primaryGenreName = entity.primaryGenreName ?: "",
                country = entity.country ?: "",
                previewUrl = entity.previewUrl ?: "",
                isFavorite = false
            )
        }
        emit(result)
    }

    override suspend fun deleteTrackFromPlaylist(trackId: Long, playlistId: Long) {
        val playlistEntity = appDatabase.playlistDao().getPlaylistByIdSuspend(playlistId) ?: return
        val playlist = playlistDbConvertor.map(playlistEntity)

        val updatedTrackIds = ArrayList(playlist.trackIds)
        updatedTrackIds.remove(trackId)

        // Обновляем текущий плейлист
        val updatedPlaylistEntity = PlaylistEntity(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverFilePath = playlist.coverFilePath,
            trackIds = gson.toJson(updatedTrackIds),
            trackCount = if (playlist.trackCount > 0) playlist.trackCount - 1 else 0
        )
        appDatabase.playlistDao().updatePlaylist(updatedPlaylistEntity)

        // Проверяем, есть ли этот трек в других плейлистах
        val allPlaylists = appDatabase.playlistDao().getPlaylistsSuspend()
        var isUsedInOtherPlaylists = false
        for (p in allPlaylists) {
            val pDomain = playlistDbConvertor.map(p)
            if (pDomain.trackIds.contains(trackId)) {
                isUsedInOtherPlaylists = true
                break
            }
        }

        // Если ни в одном плейлисте его больше нет - удаляем из общей таблицы
        if (!isUsedInOtherPlaylists) {
            appDatabase.playlistTrackDao().deleteTrack(trackId.toString())
        }
    }
    override suspend fun deletePlaylist(id: Long) {
        // Получаем плейлист перед удалением, чтобы знать, какие треки в нем были
        val playlistEntity = appDatabase.playlistDao().getPlaylistByIdSuspend(id) ?: return
        val playlist = playlistDbConvertor.map(playlistEntity)

        // Удаляем сам плейлист
        appDatabase.playlistDao().deletePlaylist(id)

        // Проверяем треки на отсутствие плейлиста
        val allPlaylists = appDatabase.playlistDao().getPlaylistsSuspend()

        playlist.trackIds.forEach { trackId ->
            var isUsedInOtherPlaylists = false
            for (p in allPlaylists) {
                val pDomain = playlistDbConvertor.map(p)
                if (pDomain.trackIds.contains(trackId)) {
                    isUsedInOtherPlaylists = true
                    break
                }
            }

            // Если трек больше нигде не используется — удаляем его из общей таблицы
            if (!isUsedInOtherPlaylists) {
                appDatabase.playlistTrackDao().deleteTrack(trackId.toString())
            }
        }
    }
}