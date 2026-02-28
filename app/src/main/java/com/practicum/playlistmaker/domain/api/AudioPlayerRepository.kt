package com.practicum.playlistmaker.domain.api

// функции плеера, которые должен реализовать слой данных
interface AudioPlayerRepository {
    fun preparePlayer(url: String, onPreparedListener: () -> Unit, onCompletionListener: () -> Unit)
    fun startPlayer()
    fun pausePlayer()
    fun release()
    fun getCurrentPosition(): Int
}