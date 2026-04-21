package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.db.AppDatabase
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.domain.models.Track
import kotlinx.coroutines.runBlocking

// сохраняет и читает список треков из sharedpreferences
class SearchHistoryRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val gson: Gson,
    private val appDatabase: AppDatabase
) : SearchHistoryRepository {

    override fun getHistory(): List<Track> {
        val json = sharedPrefs.getString(SEARCH_HISTORY_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        val history: List<Track> = gson.fromJson(json, type)

        val favoriteTracksIds = runBlocking {
            appDatabase.trackDao().getFavoriteTrackIds()
        }

        return history.map { track ->
            track.copy(isFavorite = favoriteTracksIds.contains(track.trackId))
        }
    }

    override fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPrefs.edit { putString(SEARCH_HISTORY_KEY, json) }
    }

    override fun clearHistory() {
        sharedPrefs.edit { remove(SEARCH_HISTORY_KEY) }
    }

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history_key"
    }
}