package com.practicum.playlistmaker.domain.api

// методы для открытия внешних ссылок и почты
interface ExternalNavigator {
    fun shareLink()
    fun openLink()
    fun openEmail()
}