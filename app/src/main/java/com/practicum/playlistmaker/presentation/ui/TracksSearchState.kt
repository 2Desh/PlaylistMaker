package com.practicum.playlistmaker.presentation.ui

import com.practicum.playlistmaker.domain.models.Track

// Изолированный интерфейс, описывающий возможные UI-состояния экрана поиска
sealed interface TracksSearchState {
    // Идет поиск (крутится ProgressBar)
    object Loading : TracksSearchState

    // Поиск успешен, есть результаты
    data class Content(val tracks: List<Track>) : TracksSearchState

    // История поиска (когда курсор в фокусе, а запрос пустой)
    data class History(val tracks: List<Track>) : TracksSearchState

    // Ничего не найдено
    object Empty : TracksSearchState

    // Ошибка сети
    object Error : TracksSearchState

    // Дефолтное состояние (пустой экран, если истории еще нет)
    object Default : TracksSearchState
}