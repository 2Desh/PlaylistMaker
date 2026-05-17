package com.practicum.playlistmaker.presentation.service

import com.practicum.playlistmaker.presentation.ui.PlayerState

// Список доступных команд
interface AudioPlayerControl {
    fun getPlayerState(): PlayerState
    fun setOnStateChangeListener(listener: ((PlayerState) -> Unit)?)
    fun startPlayer()
    fun pausePlayer()
    fun getCurrentPosition(): Int
    fun showNotification()
    fun hideNotification()
}