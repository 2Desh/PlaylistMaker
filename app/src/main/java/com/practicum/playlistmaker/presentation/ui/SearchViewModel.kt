package com.practicum.playlistmaker.presentation.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.models.Track

// Вьювка экрана поиска. Управляет сетевыми запросами, дебаунсом и взаимодействием с историей поиска
class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<TracksSearchState>()
    val stateLiveData: LiveData<TracksSearchState> = _stateLiveData

    private val handler = Handler(Looper.getMainLooper())

    private var latestSearchText: String? = null
    private var isClickAllowed = true

    // Задача для поиска с задержкой (Debounce)
    private val searchRunnable = Runnable {
        val newSearchText = latestSearchText ?: ""
        searchRequest(newSearchText)
    }

    // При старте проверяем, есть ли что-то в истории
    init {
        showHistory()
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) {
            return // Защита от лишних вызовов при повороте экрана
        }
        latestSearchText = changedText
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    fun searchRequest(newSearchText: String) {
        if (newSearchText.isEmpty()) return

        // Показываем ProgressBar
        _stateLiveData.postValue(TracksSearchState.Loading)

        tracksInteractor.searchTracks(newSearchText) { foundTracks, errorMessage ->
            // Ответ пришел (возможно, в фоновом потоке, поэтому используем postValue)
            if (errorMessage != null) {
                if (errorMessage == "Ничего не найдено") {
                    _stateLiveData.postValue(TracksSearchState.Empty(errorMessage))
                } else {
                    _stateLiveData.postValue(TracksSearchState.Error(errorMessage))
                }
            } else if (foundTracks != null) {
                if (foundTracks.isEmpty()) {
                    _stateLiveData.postValue(TracksSearchState.Empty("Ничего не найдено"))
                } else {
                    _stateLiveData.postValue(TracksSearchState.Content(foundTracks))
                }
            }
        }
    }

    fun showHistory() {
        handler.removeCallbacks(searchRunnable)
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
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    // Вызовется, когда пользователь нажмёт кнопку Обновить (при ошибке сети)
    fun refreshSearch() {
        val text = latestSearchText ?: ""
        searchRequest(text)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}