package com.practicum.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import androidx.room.Room
import com.google.gson.Gson
import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.data.network.NetworkClient
import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.practicum.playlistmaker.data.db.PlaylistDbConvertor
import com.practicum.playlistmaker.data.db.impl.FavoritePlaylistsRepositoryImpl
import com.practicum.playlistmaker.domain.api.FavoritePlaylistsRepository

// Создание Retrofit, API, SharedPreferences, NetworkClient, Медиаплеера и Базы Данных Room
val dataModule = module {

    // Экземпляр ITunesApi
    single<ITunesApi> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)
    }

    // SharedPreferences
    single {
        androidContext().getSharedPreferences("playlist_maker_preferences", Context.MODE_PRIVATE)
    }

    // Gson
    factory { Gson() }

    // NetworkClient. Передаём в него ITunesApi
    single<NetworkClient> {
        RetrofitNetworkClient(get())
    }

    // Медиаплеер
    factory { MediaPlayer() }

    // БД Room
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "database.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAO
    single {
        get<AppDatabase>().trackDao()
    }

    // DAO для плейлистов
    single { get<AppDatabase>().playlistDao() }

    // Конвертер
    factory { PlaylistDbConvertor(get()) }

    single { get<AppDatabase>().playlistTrackDao() }

    single<FavoritePlaylistsRepository> {
        FavoritePlaylistsRepositoryImpl(get(), get(), get())
    }
}