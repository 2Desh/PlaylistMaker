package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.api.AudioPlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Вьювка плеера. Инкапсулирует логику воспроизведения, управление таймером прогресса и форматирование времени.
class PlayerViewModel(
    private val audioPlayerInteractor: AudioPlayerInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<PlayerState>(PlayerState.Default(formatTime(DEFAULT_TIME)))
    val stateLiveData: LiveData<PlayerState> = _stateLiveData

    private var timerJob: Job? = null

    fun preparePlayer(url: String) {
        // Защита при повороте экрана
        if (_stateLiveData.value !is PlayerState.Default) {
            return
        }

        audioPlayerInteractor.preparePlayer(
            url = url,
            onPreparedListener = {
                _stateLiveData.postValue(PlayerState.Prepared(formatTime(DEFAULT_TIME)))
            },
            onCompletionListener = {
                timerJob?.cancel()
                _stateLiveData.postValue(PlayerState.Prepared(formatTime(DEFAULT_TIME)))
            }
        )
    }

    private fun startPlayer() {
        audioPlayerInteractor.startPlayer()
        _stateLiveData.postValue(PlayerState.Playing(formatTime(audioPlayerInteractor.getCurrentPosition())))
        startTimer()
    }

    fun pausePlayer() {
        audioPlayerInteractor.pausePlayer()
        timerJob?.cancel()

        val currentProgress = if (_stateLiveData.value is PlayerState.Playing) {
            (_stateLiveData.value as PlayerState.Playing).progress
        } else {
            formatTime(DEFAULT_TIME)
        }
        _stateLiveData.postValue(PlayerState.Paused(currentProgress))
    }

    // Обработка нажатия на кнопку Play/Pause
    fun playbackControl() {
        when (_stateLiveData.value) {
            is PlayerState.Playing -> pausePlayer()
            is PlayerState.Prepared, is PlayerState.Paused -> startPlayer()
            else -> Unit
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(DELAY)
                _stateLiveData.postValue(PlayerState.Playing(formatTime(audioPlayerInteractor.getCurrentPosition())))
            }
        }
    }

    // Освобождаем ресурсы
    override fun onCleared() {
        super.onCleared()
        audioPlayerInteractor.release()
    }

    private fun formatTime(timeMillis: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(timeMillis)
    }

    companion object {
        private const val DELAY = 300L
        private const val DEFAULT_TIME = 0
    }
}