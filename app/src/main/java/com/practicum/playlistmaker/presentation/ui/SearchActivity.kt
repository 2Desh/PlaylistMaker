package com.practicum.playlistmaker.presentation.ui

import android.content.Intent
import android.os.Bundle
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
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.adapters.TrackAdapter

// View-компонент поиска. Маршрутизирует действия пользователя во ViewModel и обновляет видимость элементов UI на основе TracksSearchState.
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

    // Адаптеры
    private lateinit var searchAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    // Списки для адаптеров
    private val tracks = ArrayList<Track>()
    private val historyTracks = ArrayList<Track>()

    // ViewModel
    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализируем ViewModel с помощью фабрики
        viewModel = ViewModelProvider(this, SearchViewModelFactory(this))[SearchViewModel::class.java]

        initWindowInsets()
        initViews()
        setupAdapters()
        setupListeners()

        // Подписываемся на изменения состояния
        viewModel.stateLiveData.observe(this) { state ->
            render(state)
        }
    }

    private fun initWindowInsets() {
        val rootView = findViewById<View>(R.id.search)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
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
        // Адаптер поиска
        searchAdapter = TrackAdapter(tracks) { track ->
            if (viewModel.clickDebounce()) {
                viewModel.addToHistory(track)
                openPlayer(track)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = searchAdapter

        // Адаптер для истории
        historyAdapter = TrackAdapter(historyTracks) { track ->
            if (viewModel.clickDebounce()) {
                viewModel.addToHistory(track)
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
            viewModel.showHistory()
        }

        refreshButton.setOnClickListener {
            viewModel.refreshSearch()
        }

        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }

        inputEditText.doOnTextChanged { text, _, _, _ ->
            clearIcon.isVisible = !text.isNullOrEmpty()
            val query = text?.toString() ?: ""
            if (query.isEmpty()) {
                viewModel.showHistory()
            } else {
                viewModel.searchDebounce(query)
            }
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) {
                viewModel.showHistory()
            }
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchRequest(inputEditText.text.toString())
                true
            } else false
        }
    }

    // Метод, управляющий видимостью элементов экрана
    private fun render(state: TracksSearchState) {
        recyclerView.isVisible = false
        historyLayout.isVisible = false
        placeholderNoResults.isVisible = false
        placeholderServerError.isVisible = false
        progressBar.isVisible = false

        when (state) {
            is TracksSearchState.Loading -> {
                progressBar.isVisible = true
            }
            is TracksSearchState.Content -> {
                recyclerView.isVisible = true
                tracks.clear()
                tracks.addAll(state.tracks)
                searchAdapter.notifyDataSetChanged()
            }
            is TracksSearchState.History -> {
                historyLayout.isVisible = true
                historyTracks.clear()
                historyTracks.addAll(state.tracks)
                historyAdapter.notifyDataSetChanged()
            }
            is TracksSearchState.Empty -> {
                placeholderNoResults.isVisible = true
            }
            is TracksSearchState.Error -> {
                placeholderServerError.isVisible = true
            }
            is TracksSearchState.Default -> { // комм чтобы IDE не ругалось на пустышку
            }
        }
    }

    private fun openPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.TRACK_KEY, track)
        }
        startActivity(intent)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }
}