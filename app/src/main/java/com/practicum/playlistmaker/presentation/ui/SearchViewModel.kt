package com.practicum.playlistmaker.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Вьювка экрана поиска. Управляет сетевыми запросами, дебаунсом и взаимодействием с историей поиска
class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<TracksSearchState>()
    val stateLiveData: LiveData<TracksSearchState> = _stateLiveData

    private var latestSearchText: String? = null
    private var isClickAllowed = true

    // Job для отложенного поиска
    private var searchJob: Job? = null

    // При старте проверяем, есть ли инфа в истории
    init {
        showHistory()
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) {
            return // Защита от лишних вызовов при повороте экрана
        }
        latestSearchText = changedText

        // Отменяем предыдущую задачу поиска, если юзер продолжает вводить текст
        searchJob?.cancel()

        // Запускаем новую задачу с задержкой
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            searchRequest(changedText)
        }
    }

    fun searchRequest(newSearchText: String) {
        if (newSearchText.isEmpty()) return

        _stateLiveData.postValue(TracksSearchState.Loading)

        viewModelScope.launch {
            tracksInteractor.searchTracks(newSearchText).collect { pair ->
                val foundTracks = pair.first
                val isNetworkError = pair.second

                when {
                    isNetworkError == true -> {
                        _stateLiveData.postValue(TracksSearchState.Error)
                    }
                    foundTracks != null -> {
                        if (foundTracks.isEmpty()) {
                            _stateLiveData.postValue(TracksSearchState.Empty)
                        } else {
                            _stateLiveData.postValue(TracksSearchState.Content(foundTracks))
                        }
                    }
                }
            }
        }
    }

    fun showHistory() {
        searchJob?.cancel()
        val history = searchHistoryInteractor.getHistory()
        if (history.isNotEmpty()) {
            _stateLiveData.postValue(TracksSearchState.History(history))
        } else {
            _stateLiveData.postValue(TracksSearchState.Default)
        }
    }

    fun clearHistory() {
        searchHistoryInteractor.clearHistory()
        _stateLiveData.postValue(TracksSearchState.Default)
    }

    fun addToHistory(track: Track) {
        searchHistoryInteractor.addTrackToHistory(track)
        // Если мы сейчас находимся в состоянии History, нужно сразу обновить список
        if (_stateLiveData.value is TracksSearchState.History) {
            val updatedHistory = searchHistoryInteractor.getHistory()
            _stateLiveData.postValue(TracksSearchState.History(updatedHistory))
        }
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewModelScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    // Вызовется, когда пользователь нажмёт кнопку Обновить (при ошибке сети)
    fun refreshSearch() {
        val text = latestSearchText ?: ""
        searchRequest(text)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}