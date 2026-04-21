package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.api.AudioPlayerInteractor
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsInteractor
import com.practicum.playlistmaker.domain.api.FavoriteTracksInteractor
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Вьювка плеера. Инкапсулирует логику воспроизведения, управление таймером прогресса и форматирование времени.
class PlayerViewModel(
    private val audioPlayerInteractor: AudioPlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistsInteractor: FavoritePlaylistsInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<PlayerState>(PlayerState.Default(formatTime(DEFAULT_TIME)))
    val stateLiveData: LiveData<PlayerState> = _stateLiveData

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _playlistsLiveData = MutableLiveData<List<Playlist>>()
    val playlistsLiveData: LiveData<List<Playlist>> = _playlistsLiveData

    private val _isAddedToPlaylist = MutableLiveData<Boolean>()
    val isAddedToPlaylist: LiveData<Boolean> = _isAddedToPlaylist

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private var timerJob: Job? = null

    private var currentTrackId: Long? = null

    init {
        viewModelScope.launch {
            playlistsInteractor.getPlaylists().collect { playlists ->
                _playlistsLiveData.postValue(playlists)
                checkTrackInPlaylists(playlists)
            }
        }
    }

    fun setTrackId(trackId: Long) {
        currentTrackId = trackId
        checkTrackInPlaylists(_playlistsLiveData.value)
    }

    private fun checkTrackInPlaylists(playlists: List<Playlist>?) {
        if (playlists == null || currentTrackId == null) return

        val isAdded = playlists.any { it.trackIds.contains(currentTrackId) }
        _isAddedToPlaylist.postValue(isAdded)
    }

    fun preparePlayer(url: String) {
        if (_stateLiveData.value !is PlayerState.Default) return

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

    fun checkIsFavorite(isFavorite: Boolean) {
        _isFavorite.value = isFavorite
    }

    fun onFavoriteClicked(track: Track) {
        viewModelScope.launch {
            val isFav = _isFavorite.value ?: false
            if (isFav) {
                favoriteTracksInteractor.deleteTrack(track.trackId)
                _isFavorite.postValue(false)
            } else {
                favoriteTracksInteractor.insertTrack(track)
                _isFavorite.postValue(true)
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        viewModelScope.launch {
            val isAdded = playlistsInteractor.addTrackToPlaylist(track, playlist)
            if (isAdded) {
                _toastMessage.postValue("Добавлено в плейлист ${playlist.name}")
            } else {
                _toastMessage.postValue("Трек уже добавлен в плейлист ${playlist.name}")
            }
        }
    }

    fun toastMessageShown() {
        _toastMessage.value = ""
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