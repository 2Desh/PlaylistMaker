package com.practicum.playlistmaker.presentation.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.api.AudioPlayerInteractor
import java.text.SimpleDateFormat
import java.util.Locale

// Вьювка плеера. Инкапсулирует логику воспроизведения, управление таймером прогресса и форматирование времени.
class PlayerViewModel(
    private val audioPlayerInteractor: AudioPlayerInteractor
) : ViewModel() {

    // _stateLiveData - приватная, её может изменять только сама ViewModel
    private val _stateLiveData = MutableLiveData<PlayerState>(PlayerState.Default)
    // stateLiveData - публичная, Activity может только подписываться на неё, но не менять
    val stateLiveData: LiveData<PlayerState> = _stateLiveData

    // Переносим Handler сюда, чтобы отвязать таймер от жизненного цикла Activity
    private val handler = Handler(Looper.getMainLooper())

    // Задача обновления таймера
    private val timerRunnable = object : Runnable {
        override fun run() {
            // Обновляем таймер только если состояние Playing
            if (_stateLiveData.value is PlayerState.Playing) {
                val currentPosition = audioPlayerInteractor.getCurrentPosition()
                _stateLiveData.postValue(PlayerState.Playing(formatTime(currentPosition)))
                handler.postDelayed(this, DELAY)
            }
        }
    }

    fun preparePlayer(url: String) {
        audioPlayerInteractor.preparePlayer(
            url = url,
            onPreparedListener = {
                // Плеер готов, отправляем состояние Prepared
                _stateLiveData.postValue(PlayerState.Prepared)
            },
            onCompletionListener = {
                // Трек доиграл до конца, сбрасываем состояние в Prepared и останавливаем таймер
                _stateLiveData.postValue(PlayerState.Prepared)
                handler.removeCallbacks(timerRunnable)
            }
        )
    }

    private fun startPlayer() {
        audioPlayerInteractor.startPlayer()
        _stateLiveData.postValue(PlayerState.Playing(formatTime(audioPlayerInteractor.getCurrentPosition())))
        handler.post(timerRunnable)
    }

    fun pausePlayer() {
        audioPlayerInteractor.pausePlayer()
        val currentProgress = if (_stateLiveData.value is PlayerState.Playing) {
            (_stateLiveData.value as PlayerState.Playing).progress
        } else {
            "00:00"
        }
        _stateLiveData.postValue(PlayerState.Paused(currentProgress))
        handler.removeCallbacks(timerRunnable) // Останавливаем таймер
    }

    // Обработка нажатия на кнопку Play/Pause
    fun playbackControl() {
        when (_stateLiveData.value) {
            is PlayerState.Playing -> pausePlayer()
            is PlayerState.Prepared, is PlayerState.Paused -> startPlayer()
            else -> {}
        }
    }

    // Освобождаем ресурсы
    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(timerRunnable)
        audioPlayerInteractor.release()
    }

    private fun formatTime(timeMillis: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(timeMillis)
    }

    companion object {
        private const val DELAY = 300L
    }
}