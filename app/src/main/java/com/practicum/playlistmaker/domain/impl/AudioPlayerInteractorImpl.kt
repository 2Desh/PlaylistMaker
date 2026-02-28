package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.AudioPlayerInteractor
import com.practicum.playlistmaker.domain.api.AudioPlayerRepository

// связующее звено между экраном плеера и репозиторием
class AudioPlayerInteractorImpl(private val repository: AudioPlayerRepository) : AudioPlayerInteractor {

    override fun preparePlayer(url: String, onPreparedListener: () -> Unit, onCompletionListener: () -> Unit) {
        repository.preparePlayer(url, onPreparedListener, onCompletionListener)
    }

    override fun startPlayer() {
        repository.startPlayer()
    }

    override fun pausePlayer() {
        repository.pausePlayer()
    }

    override fun release() {
        repository.release()
    }

    override fun getCurrentPosition(): Int {
        return repository.getCurrentPosition()
    }
}