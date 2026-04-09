package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.repository.*
import com.practicum.playlistmaker.domain.api.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Описание всех интеракторов
val repositoryModule = module {

    single<TracksRepository> { TracksRepositoryImpl(get()) }

    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get(), get()) }

    single<SettingsRepository> { SettingsRepositoryImpl(get(), androidContext()) }

    single<ExternalNavigator> { ExternalNavigatorImpl(androidContext()) }

    factory<AudioPlayerRepository> { AudioPlayerRepositoryImpl(get()) }
}