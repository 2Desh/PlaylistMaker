package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.repository.FavoriteTracksRepositoryImpl
import com.practicum.playlistmaker.domain.api.*
import com.practicum.playlistmaker.domain.impl.*
import org.koin.dsl.module
import com.practicum.playlistmaker.domain.impl.TracksInteractorImpl

// Описание всех репозиториев проекта
val interactorModule = module {

    single<TracksInteractor> { TracksInteractorImpl(get()) }

    single<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }

    single<SettingsInteractor> { SettingsInteractorImpl(get()) }

    single<SharingInteractor> { SharingInteractorImpl(get()) }

    factory<AudioPlayerInteractor> { AudioPlayerInteractorImpl(get()) }

    single<FavoriteTracksRepository> { FavoriteTracksRepositoryImpl(get(), get()) }

    single<FavoriteTracksInteractor> { FavoriteTracksInteractorImpl(get()) }


}