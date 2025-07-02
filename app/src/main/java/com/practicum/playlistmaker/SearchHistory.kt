package com.practicum.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

class SearchHistory(private val sharedPrefs: SharedPreferences, private val gson: Gson) {

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history_key"
        private const val MAX_HISTORY_SIZE = 10
    }

    // Метод для добавления трека в историю
    fun addTrackToHistory(newTrack: Track) {
        val history = getHistory().toMutableList()
        val existingTrackIndex = history.indexOfFirst { it.trackId == newTrack.trackId }

        if (existingTrackIndex != -1) {
            // Если трек уже есть, удаляем старую запись
            history.removeAt(existingTrackIndex)
        }

        // Добавляем новый трек в начало списка
        history.add(0, newTrack)

        // Если список превышает лимит, удаляем старый трек
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeLast()
        }
        saveHistory(history)
    }

    // Метод для получения истории поиска
    fun getHistory(): List<Track> {
        val json = sharedPrefs.getString(SEARCH_HISTORY_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    // Метод для сохранения истории поиска
    private fun saveHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPrefs.edit { putString(SEARCH_HISTORY_KEY, json) }
    }

    // Метод для очистки истории
    fun clearHistory() {
        sharedPrefs.edit { remove(SEARCH_HISTORY_KEY) }
    }
}