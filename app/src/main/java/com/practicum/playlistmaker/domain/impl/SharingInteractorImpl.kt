package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.ExternalNavigator
import com.practicum.playlistmaker.domain.api.SharingInteractor

// логика взаимодействия с внешним навигатором
class SharingInteractorImpl(private val externalNavigator: ExternalNavigator) : SharingInteractor {
    override fun shareApp() {
        externalNavigator.shareLink()
    }

    override fun openTerms() {
        externalNavigator.openLink()
    }

    override fun openSupport() {
        externalNavigator.openEmail()
    }
}