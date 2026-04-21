package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.db.TrackDbConvertor // <-- Добавлен нужный импорт
import com.practicum.playlistmaker.data.repository.*
import com.practicum.playlistmaker.domain.api.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// Описание всех репозиториев
val repositoryModule = module {

    single<TracksRepository> { TracksRepositoryImpl(get(), get()) }

    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get(), get(), get()) }

    single<SettingsRepository> { SettingsRepositoryImpl(get(), androidContext()) }

    single<ExternalNavigator> { ExternalNavigatorImpl(androidContext()) }

    factory<AudioPlayerRepository> { AudioPlayerRepositoryImpl(get()) }

    factory { TrackDbConvertor() } // Конвертер для БД

    single<FavoriteTracksRepository> { FavoriteTracksRepositoryImpl(get(), get()) } // Репозиторий избранных треков

    single<FavoritePlaylistsRepository> { FavoritePlaylistsRepositoryImpl(get(), get(), get()) }
}