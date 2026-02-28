package com.practicum.playlistmaker.data.repository

import android.media.MediaPlayer
import com.practicum.playlistmaker.domain.api.AudioPlayerRepository

// управляет системным медиаплеером
class AudioPlayerRepositoryImpl : AudioPlayerRepository {

    private var mediaPlayer = MediaPlayer()

    override fun preparePlayer(url: String, onPreparedListener: () -> Unit, onCompletionListener: () -> Unit) {
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                onPreparedListener.invoke()
            }
            mediaPlayer.setOnCompletionListener {
                onCompletionListener.invoke()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun startPlayer() {
        mediaPlayer.start()
    }

    override fun pausePlayer() {
        mediaPlayer.pause()
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }
}