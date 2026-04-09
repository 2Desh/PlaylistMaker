package com.practicum.playlistmaker.presentation.ui

// Изолированный класс, определяющий конечное UI-состояние экрана аудиоплеера
sealed class PlayerState(
    val isPlayButtonEnabled: Boolean,
    val buttonText: String,
    val progress: String
) {
    object Default : PlayerState(isPlayButtonEnabled = false, buttonText = "PLAY", progress = "00:00")

    object Prepared : PlayerState(isPlayButtonEnabled = true, buttonText = "PLAY", progress = "00:00")

    class Playing(progress: String) : PlayerState(isPlayButtonEnabled = true, buttonText = "PAUSE", progress = progress)

    class Paused(progress: String) : PlayerState(isPlayButtonEnabled = true, buttonText = "PLAY", progress = progress)
}