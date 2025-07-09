package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var clearIcon: ImageView

    private var searchText: String = ""

    private lateinit var recyclerView: RecyclerView // RecyclerView для результатов поиска
    private val tracks = ArrayList<Track>() // список треков

    private lateinit var historyLayout: LinearLayout // для истории поиска
    private lateinit var historyRecyclerView: RecyclerView // RecyclerView для истории поиска
    private lateinit var clearHistoryButton: Button // Кнопка очистки истории

    private lateinit var iTunesApiService: iTunesApi

    private lateinit var placeholderNoResults: LinearLayout
    private lateinit var placeholderServerError: LinearLayout
    private lateinit var refreshButton: Button

    private lateinit var searchHistory: SearchHistory

    private val historyTracks = ArrayList<Track>()              // список треков для истории поиска
    private lateinit var historyAdapter: TrackAdapter

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { performSearch(inputEditText.text.toString()) }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация SearchHistory из App класса
        searchHistory = (applicationContext as App).searchHistory
        historyTracks.addAll(searchHistory.getHistory())
        inputEditText = findViewById(R.id.inputEditText)
        clearIcon = findViewById(R.id.clearIcon)
        recyclerView = findViewById(R.id.recyclerView)
        placeholderNoResults = findViewById(R.id.placeholderNoResults)
        placeholderServerError = findViewById(R.id.placeholderServerError)
        refreshButton = findViewById(R.id.refreshButton)
        historyLayout = findViewById(R.id.historyLayout)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)

        val toolbar = findViewById<MaterialToolbar>(R.id.title_search)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        iTunesApiService = NetworkClient.iTunesApiService

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TrackAdapter(tracks) { track ->
            // Добавляем трек в историю
            searchHistory.addTrackToHistory(track)
            historyTracks.clear()
            historyTracks.addAll(searchHistory.getHistory())
            historyAdapter.notifyDataSetChanged()

            // Переход на экран аудиоплеера
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.TRACK_KEY, track)
            startActivity(intent)
        }

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(historyTracks) { track ->
            // Добавляем трек в историю
            searchHistory.addTrackToHistory(track)
            historyTracks.clear()
            historyTracks.addAll(searchHistory.getHistory())
            historyAdapter.notifyDataSetChanged()

            // Заполняем строку поиска и запускаем поиск
            inputEditText.setText(track.trackName)
            performSearch(track.trackName)

            // Переход на экран аудиоплеера
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.TRACK_KEY, track)
            startActivity(intent)
        }
        historyRecyclerView.adapter = historyAdapter

        clearIcon.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            tracks.clear()
            (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
            hideAllPlaceholders()
            if (historyTracks.isNotEmpty()) {
                historyLayout.isVisible = true
                recyclerView.isVisible = false
            }
        }

        refreshButton.setOnClickListener {
            performSearch(inputEditText.text.toString())
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearIcon.isVisible = !s.isNullOrEmpty()
                searchText = s.toString()
                hideAllPlaceholders()

                if (s.isNullOrEmpty() && historyTracks.isNotEmpty()) {
                    historyLayout.isVisible = true
                    recyclerView.isVisible = false
                } else {
                    historyLayout.isVisible = false
                    recyclerView.isVisible = true
                }

                handler.removeCallbacks(searchRunnable)
                if (!s.isNullOrEmpty()) {
                    handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
                }
            }

            override fun afterTextChanged(s: Editable?) { }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty() && historyTracks.isNotEmpty()) {
                historyLayout.isVisible = true
                recyclerView.isVisible = false
            } else {
                historyLayout.isVisible = false
                recyclerView.isVisible = true
            }
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            historyTracks.clear()
            historyAdapter.notifyDataSetChanged()
            historyLayout.isVisible = false
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(searchRunnable)
                performSearch(inputEditText.text.toString())
                true
            } else false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        inputEditText.setText(searchText)
        if (searchText.isNotEmpty()) {
            historyLayout.isVisible = false
            recyclerView.isVisible = true
        } else if (historyTracks.isNotEmpty()) {
            historyLayout.isVisible = true
            recyclerView.isVisible = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            tracks.clear()
            (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
            hideAllPlaceholders()
            if (historyTracks.isNotEmpty()) {
                historyLayout.isVisible = true
                recyclerView.isVisible = false
            }
            return
        }

        tracks.clear()
        (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
        hideAllPlaceholders()
        recyclerView.isVisible = true

        val call = iTunesApiService.search(query)

        call.enqueue(object : Callback<ITunesResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<ITunesResponse>, response: Response<ITunesResponse>) {
                if (response.isSuccessful) {
                    val iTunesResponse = response.body()
                    if (iTunesResponse != null && iTunesResponse.results.isNotEmpty()) {
                        tracks.addAll(iTunesResponse.results.map {
                            Track(
                                trackName = it.trackName ?: getString(R.string.unknown_track_name),
                                trackId = it.trackId,
                                artistName = it.artistName ?: getString(R.string.unknown_artist_name),
                                trackTime = it.trackTimeMillis,
                                artworkUrl100 = it.artworkUrl100 ?: "",
                                collectionName = it.collectionName,
                                releaseDate = it.releaseDate,
                                primaryGenreName = it.primaryGenreName,
                                country = it.country
                            )
                        })
                        (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                        hideAllPlaceholders()
                    } else {
                        tracks.clear()
                        (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                        recyclerView.isVisible = false
                        placeholderNoResults.isVisible = true
                        placeholderServerError.isVisible = false
                        Log.d(TAG, "Нет результатов по запросу: $query")
                    }
                } else {
                    tracks.clear()
                    (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                    recyclerView.isVisible = false
                    placeholderServerError.isVisible = true
                    placeholderNoResults.isVisible = false
                    Log.e(TAG, "Ошибка сервера: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                tracks.clear()
                (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                recyclerView.isVisible = false
                placeholderServerError.isVisible = true
                placeholderNoResults.isVisible = false
                Log.e(TAG, "Ошибка запроса: ${t.message}", t)
            }
        })
    }

    private fun hideAllPlaceholders() {
        placeholderNoResults.isVisible = false
        placeholderServerError.isVisible = false
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    companion object {
        const val SEARCH_TEXT_KEY = "searchText"
        const val TAG = "SearchActivity"

        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
