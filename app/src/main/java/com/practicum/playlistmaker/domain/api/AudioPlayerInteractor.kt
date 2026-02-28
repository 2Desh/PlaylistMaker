package com.practicum.playlistmaker.domain.api

// управление воспроизведением для UI
interface AudioPlayerInteractor {
    fun preparePlayer(url: String, onPreparedListener: () -> Unit, onCompletionListener: () -> Unit)
    fun startPlayer()
    fun pausePlayer()
    fun release()
    fun getCurrentPosition(): Int
}