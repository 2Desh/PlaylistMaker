package com.practicum.playlistmaker.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.adapters.TrackAdapter

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var clearIcon: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyLayout: LinearLayout
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var placeholderNoResults: LinearLayout
    private lateinit var placeholderServerError: LinearLayout
    private lateinit var refreshButton: Button
    private lateinit var progressBar: ProgressBar

    private var searchText: String = ""
    private val tracks = ArrayList<Track>()
    private val historyTracks = ArrayList<Track>()

    private lateinit var historyAdapter: TrackAdapter
    private val tracksInteractor = Creator.provideTracksInteractor()
    private lateinit var searchHistoryInteractor: SearchHistoryInteractor

    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { performSearch(inputEditText.text.toString()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initWindowInsets()
        initInteractors()
        initViews()
        setupAdapters()
        setupListeners()
    }

    private fun initWindowInsets() {
        val rootView = findViewById<View>(R.id.search)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
    }

    private fun initInteractors() {
        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(this)
        historyTracks.addAll(searchHistoryInteractor.getHistory())
    }

    private fun initViews() {
        inputEditText = findViewById(R.id.inputEditText)
        clearIcon = findViewById(R.id.clearIcon)
        recyclerView = findViewById(R.id.recyclerView)
        placeholderNoResults = findViewById(R.id.placeholderNoResults)
        placeholderServerError = findViewById(R.id.placeholderServerError)
        refreshButton = findViewById(R.id.refreshButton)
        progressBar = findViewById(R.id.progressBar)
        historyLayout = findViewById(R.id.historyLayout)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
    }

    private fun setupAdapters() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TrackAdapter(tracks) { track ->
            if (clickDebounce()) {
                saveToHistory(track)
                openPlayer(track)
            }
        }

        historyAdapter = TrackAdapter(historyTracks) { track ->
            if (clickDebounce()) {
                saveToHistory(track)
                openPlayer(track)
            }
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        val toolbar = findViewById<MaterialToolbar>(R.id.title_search)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        clearIcon.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            handler.removeCallbacks(searchRunnable)
            tracks.clear()
            recyclerView.adapter?.notifyDataSetChanged()
            hideAllPlaceholders()
            showHistoryIfPossible()
        }

        refreshButton.setOnClickListener { performSearch(inputEditText.text.toString()) }

        clearHistoryButton.setOnClickListener {
            searchHistoryInteractor.clearHistory()
            historyTracks.clear()
            historyAdapter.notifyDataSetChanged()
            historyLayout.isVisible = false
        }

        inputEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {} // шаблонный код
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearIcon.isVisible = !s.isNullOrEmpty()
                searchText = s.toString()
                hideAllPlaceholders()

                if (s.isNullOrEmpty()) {
                    handler.removeCallbacks(searchRunnable)
                    showHistoryIfPossible()
                } else {
                    historyLayout.isVisible = false
                    searchDebounce()
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {} // шаблонный код
        })

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) showHistoryIfPossible()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(searchRunnable)
                performSearch(inputEditText.text.toString())
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        progressBar.isVisible = true
        recyclerView.isVisible = false
        hideAllPlaceholders()

        tracksInteractor.searchTracks(query) { foundTracks, errorMessage ->
            runOnUiThread {
                handleSearchResult(foundTracks, errorMessage)
            }
        }
    }

    private fun handleSearchResult(foundTracks: List<Track>?, errorMessage: String?) {
        progressBar.isVisible = false

        if (foundTracks != null) {
            tracks.clear()
            tracks.addAll(foundTracks)
            recyclerView.adapter?.notifyDataSetChanged()
            recyclerView.isVisible = tracks.isNotEmpty()
            if (tracks.isEmpty()) placeholderNoResults.isVisible = true
        }

        if (errorMessage != null) {
            if (errorMessage == "Ничего не найдено") {
                placeholderNoResults.isVisible = true
            } else {
                placeholderServerError.isVisible = true
            }
        }
    }

    private fun saveToHistory(track: Track) {
        searchHistoryInteractor.addTrackToHistory(track)
        historyTracks.clear()
        historyTracks.addAll(searchHistoryInteractor.getHistory())
        historyAdapter.notifyDataSetChanged()
    }

    private fun showHistoryIfPossible() {
        if (historyTracks.isNotEmpty()) {
            historyLayout.isVisible = true
            recyclerView.isVisible = false
        }
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun openPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.TRACK_KEY, track)
        }
        startActivity(intent)
    }

    private fun hideAllPlaceholders() {
        placeholderNoResults.isVisible = false
        placeholderServerError.isVisible = false
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
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
        } else {
            showHistoryIfPossible()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        const val SEARCH_TEXT_KEY = "searchText"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}