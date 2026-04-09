package com.practicum.playlistmaker.presentation.ui

// Изолированный класс, определяющий конечное UI-состояние экрана аудиоплеера
sealed class PlayerState(
    val isPlayButtonEnabled: Boolean,
    val buttonText: String,
    val progress: String
) {
    class Default(progress: String) : PlayerState(isPlayButtonEnabled = false, buttonText = STATE_PLAY, progress = progress)

    class Prepared(progress: String) : PlayerState(isPlayButtonEnabled = true, buttonText = STATE_PLAY, progress = progress)

    class Playing(progress: String) : PlayerState(isPlayButtonEnabled = true, buttonText = STATE_PAUSE, progress = progress)

    class Paused(progress: String) : PlayerState(isPlayButtonEnabled = true, buttonText = STATE_PLAY, progress = progress)

    companion object {
        const val STATE_PLAY = "PLAY"
        const val STATE_PAUSE = "PAUSE"
    }
}