package com.practicum.playlistmaker.domain.api

// команды для кнопок "поделиться" и "поддержка"
interface SharingInteractor {
    fun shareApp()
    fun openTerms()
    fun openSupport()
}