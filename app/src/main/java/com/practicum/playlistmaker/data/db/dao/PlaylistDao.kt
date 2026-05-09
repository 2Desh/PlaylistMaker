package com.practicum.playlistmaker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.practicum.playlistmaker.data.db.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

// Интерфейс доступа к БД
@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists_table ORDER BY id DESC")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    // Для подписки на изменения конкретного плейлиста
    @Query("SELECT * FROM playlists_table WHERE id = :id")
    fun getPlaylistById(id: Long): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists_table WHERE id = :id")
    suspend fun getPlaylistByIdSuspend(id: Long): PlaylistEntity?

    @Query("SELECT * FROM playlists_table")
    suspend fun getPlaylistsSuspend(): List<PlaylistEntity>

    @Query("DELETE FROM playlists_table WHERE id = :id")
    suspend fun deletePlaylist(id: Long)
}