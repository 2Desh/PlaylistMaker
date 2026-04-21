package com.practicum.playlistmaker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.practicum.playlistmaker.data.db.entity.PlaylistTrackEntity

// DAO треков плейлиста
@Dao
interface PlaylistTrackDao {

    // Сохраняем трек. Если такой уже есть, игнорируем
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity)

    // Получение треков по списку ID
    @Query("SELECT * FROM playlist_tracks_table")
    suspend fun getAllTracks(): List<PlaylistTrackEntity>
}