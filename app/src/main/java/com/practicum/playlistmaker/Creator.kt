package com.practicum.playlistmaker

import android.content.Context
import com.google.gson.Gson
import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.data.repository.ExternalNavigatorImpl
import com.practicum.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.repository.AudioPlayerRepositoryImpl
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.SettingsInteractor
import com.practicum.playlistmaker.domain.api.SharingInteractor
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.api.AudioPlayerInteractor
import com.practicum.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.domain.impl.SharingInteractorImpl
import com.practicum.playlistmaker.domain.impl.TracksInteractorImpl
import com.practicum.playlistmaker.domain.impl.AudioPlayerInteractorImpl

object Creator {
    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        val sharedPreferences = context.getSharedPreferences("playlist_maker_preferences", Context.MODE_PRIVATE)
        return SearchHistoryInteractorImpl(SearchHistoryRepositoryImpl(sharedPreferences, Gson()))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        val sharedPreferences = context.getSharedPreferences("playlist_maker_preferences", Context.MODE_PRIVATE)
        return SettingsInteractorImpl(SettingsRepositoryImpl(sharedPreferences, context))
    }

    fun provideSharingInteractor(context: Context): SharingInteractor {
        return SharingInteractorImpl(ExternalNavigatorImpl(context))
    }

    fun provideAudioPlayerInteractor(): AudioPlayerInteractor {
        return AudioPlayerInteractorImpl(AudioPlayerRepositoryImpl())
    }
}