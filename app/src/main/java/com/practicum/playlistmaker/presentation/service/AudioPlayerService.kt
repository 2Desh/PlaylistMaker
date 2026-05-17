package com.practicum.playlistmaker.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.ui.PlayerState
import java.text.SimpleDateFormat
import java.util.Locale

// Сервис Плеера
class AudioPlayerService : Service(), AudioPlayerControl {

    private val binder = AudioPlayerBinder()
    private var mediaPlayer: MediaPlayer? = null

    // Данные для уведомления
    private var trackUrl: String? = null
    private var trackTitle: String = ""
    private var trackArtist: String = ""

    private var playerState: PlayerState = PlayerState.Default("00:00")
    private var stateListener: ((PlayerState) -> Unit)? = null

    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerControl = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        trackUrl = intent?.getStringExtra("URL")
        trackTitle = intent?.getStringExtra("TITLE") ?: "Unknown"
        trackArtist = intent?.getStringExtra("ARTIST") ?: "Unknown"

        preparePlayer()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        releasePlayer()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun preparePlayer() {
        if (trackUrl.isNullOrEmpty()) return

        try {
            mediaPlayer?.apply {
                reset()
                setDataSource(trackUrl)
                prepareAsync()
                setOnPreparedListener {
                    updateState(PlayerState.Prepared("00:00"))
                }
                setOnCompletionListener {
                    updateState(PlayerState.Prepared("00:00"))
                    hideNotification()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun startPlayer() {
        mediaPlayer?.start()
        updateState(PlayerState.Playing(formatTime(getCurrentPosition())))
    }

    override fun pausePlayer() {
        mediaPlayer?.pause()
        updateState(PlayerState.Paused(formatTime(getCurrentPosition())))
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        updateState(PlayerState.Default("00:00"))
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun getPlayerState(): PlayerState = playerState

    override fun setOnStateChangeListener(listener: ((PlayerState) -> Unit)?) {
        stateListener = listener
        listener?.invoke(playerState)
    }

    private fun updateState(newState: PlayerState) {
        playerState = newState
        stateListener?.invoke(newState)
    }

    // Логика Foreground

    override fun showNotification() {
        // Показывается уведомление если идёт воспроизведение в фоне
        if (playerState !is PlayerState.Playing) return

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playlist Maker")
            .setContentText("$trackArtist - $trackTitle")
            .setSmallIcon(R.drawable.ic_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )
    }

    override fun hideNotification() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Player Background",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(timeMillis: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(timeMillis)
    }

    companion object {
        private const val CHANNEL_ID = "audio_player_channel"
        private const val NOTIFICATION_ID = 101
    }
}