package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsInteractor
import com.practicum.playlistmaker.domain.api.FavoriteTracksInteractor
import com.practicum.playlistmaker.domain.models.Playlist
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.service.AudioPlayerControl
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Вьювка плеера. Инкапсулирует логику воспроизведения, управление таймером прогресса и форматирование времени.
class PlayerViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistsInteractor: FavoritePlaylistsInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<PlayerState>(PlayerState.Default("00:00"))
    val stateLiveData: LiveData<PlayerState> = _stateLiveData

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _playlistsLiveData = MutableLiveData<List<Playlist>>()
    val playlistsLiveData: LiveData<List<Playlist>> = _playlistsLiveData

    private val _isAddedToPlaylist = MutableLiveData<Boolean>()
    val isAddedToPlaylist: LiveData<Boolean> = _isAddedToPlaylist

    private val _toastMessage = MutableLiveData<Pair<Int, String>?>()
    val toastMessage: LiveData<Pair<Int, String>?> = _toastMessage

    private var timerJob: Job? = null
    private var currentTrackId: Long? = null

    private var audioPlayerControl: AudioPlayerControl? = null

    init {
        viewModelScope.launch {
            playlistsInteractor.getPlaylists().collect { playlists ->
                _playlistsLiveData.postValue(playlists)
                checkTrackInPlaylists(playlists)
            }
        }
    }

    fun setAudioPlayerControl(control: AudioPlayerControl) {
        audioPlayerControl = control
        control.setOnStateChangeListener { state ->
            _stateLiveData.postValue(state)
            if (state is PlayerState.Playing) {
                startTimer()
            } else {
                timerJob?.cancel()
            }
        }
    }

    fun removeAudioPlayerControl() {
        audioPlayerControl?.setOnStateChangeListener(null)
        audioPlayerControl = null
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

    // Обработка нажатия на кнопку Play/Pause
    fun playbackControl() {
        when (_stateLiveData.value) {
            is PlayerState.Playing -> audioPlayerControl?.pausePlayer()
            is PlayerState.Prepared, is PlayerState.Paused -> audioPlayerControl?.startPlayer()
            else -> Unit
        }
    }

    fun onAppBackgrounded() {
        audioPlayerControl?.showNotification()
    }

    fun onAppForegrounded() {
        audioPlayerControl?.hideNotification()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(DELAY)
                val currentPos = audioPlayerControl?.getCurrentPosition() ?: 0
                _stateLiveData.postValue(PlayerState.Playing(formatTime(currentPos)))
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
                _toastMessage.postValue(Pair(R.string.added_to_playlist, playlist.name))
            } else {
                _toastMessage.postValue(Pair(R.string.already_added_to_playlist, playlist.name))
            }
        }
    }

    fun toastMessageShown() {
        _toastMessage.value = null
    }

    private fun formatTime(timeMillis: Int): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(timeMillis)
    }

    companion object {
        private const val DELAY = 300L
    }
}