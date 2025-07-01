package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
import java.text.SimpleDateFormat
import java.util.Locale
import android.os.Handler
import android.os.Looper

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var clearIcon: ImageView

    private var searchText: String = ""

    private lateinit var recyclerView: RecyclerView // RecyclerView для результатов поиска
    private val tracks = ArrayList<Track>() // список треков

    private lateinit var historyLayout: LinearLayout // для истории поиска (ИСПРАВЛЕНО ИМЯ)
    private lateinit var historyRecyclerView: RecyclerView // RecyclerView для истории поиска
    private lateinit var clearHistoryButton: Button // Кнопка очистки истории

    private lateinit var iTunesApiService: iTunesApi

    private lateinit var placeholderNoResults: LinearLayout
    private lateinit var placeholderServerError: LinearLayout
    private lateinit var refreshButton: Button

    private lateinit var searchHistory: SearchHistory

    private val historyTracks = ArrayList<Track>()              // список треков для истории поиска
    // !!! АДАПТЕР ДЛЯ ИСТОРИИ ПОИСКА !!!
    private lateinit var historyAdapter: TrackAdapter

    // Handler для дебаунса
    private val handler = Handler(Looper.getMainLooper())
    // Runnable для дебаунса
    private val searchRunnable = Runnable { performSearch(inputEditText.text.toString()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // иницилизация SearchHistory из App класса
        searchHistory = (applicationContext as App).searchHistory
        // история при создании активности
        historyTracks.addAll(searchHistory.getHistory())


        // view элементы
        inputEditText = findViewById(R.id.inputEditText)
        clearIcon = findViewById(R.id.clearIcon)
        recyclerView = findViewById(R.id.recyclerView)
        placeholderNoResults = findViewById(R.id.placeholderNoResults)
        placeholderServerError = findViewById(R.id.placeholderServerError)
        refreshButton = findViewById(R.id.refreshButton)

        // новые вью для истории
        historyLayout = findViewById(R.id.historyLayout) // ИСПРАВЛЕНО ИМЯ
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)

        // Настройка Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.title_search)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // API
        iTunesApiService = NetworkClient.iTunesApiService

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TrackAdapter(tracks) { track ->
            // добавляем трек в историю
            searchHistory.addTrackToHistory(track)
            historyTracks.clear()
            historyTracks.addAll(searchHistory.getHistory())
            historyAdapter.notifyDataSetChanged()
            // переход на другой экран
        }

        // адаптер для истории
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(historyTracks) { track ->
            // добавляем трек в историю
            searchHistory.addTrackToHistory(track)
            historyTracks.clear()
            historyTracks.addAll(searchHistory.getHistory())
            historyAdapter.notifyDataSetChanged()

            // поиск по имени
            inputEditText.setText(track.trackName)
            performSearch(track.trackName)
        }
        historyRecyclerView.adapter = historyAdapter


        // обработчик кнопки очистки поля ввода
        clearIcon.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            tracks.clear()
            (recyclerView.adapter as TrackAdapter).notifyDataSetChanged() // обновляем список
            hideAllPlaceholders()
            // показываем историю если есть что показывать
            if (historyTracks.isNotEmpty()) {
                historyLayout.isVisible = true // ИСПРАВЛЕНО ИМЯ
                recyclerView.isVisible = false
            }
        }

        // Обработчик кнопки рефреш на заглушке ошибки сети
        refreshButton.setOnClickListener {
            performSearch(inputEditText.text.toString())
        }

        // для отслеживания изменений текста в поле ввода
        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearIcon.isVisible = !s.isNullOrEmpty()
                searchText = s.toString()
                hideAllPlaceholders() // скрываем все заглушки

                // логика отображения или скрытия истории и результатов поиска
                if (s.isNullOrEmpty() && historyTracks.isNotEmpty()) {
                    historyLayout.isVisible = true // ИСПРАВЛЕНО ИМЯ
                    recyclerView.isVisible = false // скрываем результаты поиска
                } else {
                    historyLayout.isVisible = false // ИСПРАВЛЕНО ИМЯ
                    recyclerView.isVisible = true
                }

                // удаляем предыдущие запросы и планируем новый дебаунс
                handler.removeCallbacks(searchRunnable)
                if (!s.isNullOrEmpty()) { // новый дебаунс если текст не пустой
                    handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        // обработчик для поля ввода
        inputEditText.setOnFocusChangeListener { view, hasFocus ->
            // если поле ввода активно, пустой текст и есть история поиска, показываем историю
            if (hasFocus && inputEditText.text.isEmpty() && historyTracks.isNotEmpty()) {
                historyLayout.isVisible = true // ИСПРАВЛЕНО ИМЯ
                recyclerView.isVisible = false // скрываем результаты
            } else {
                historyLayout.isVisible = false // ИСПРАВЛЕНО ИМЯ
                recyclerView.isVisible = true // результаты
            }
        }

        // обработка кнопы очистки истории
        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            historyTracks.clear() // очищаем список в активности
            historyAdapter.notifyDataSetChanged() // уведомляем адаптер
            historyLayout.isVisible = false // ИСПРАВЛЕНО ИМЯ
        }

        // Обработчик кнопки на клавиатуре
        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // если все хорошо, то поиск без дебаунса
                handler.removeCallbacks(searchRunnable) // очищаем дебаунс запросы
                performSearch(inputEditText.text.toString())
                true
            }
            false
        }
    }

    // Сохранение состояния при повороте экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    // Восстановление состояния после поворота экрана
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        inputEditText.setText(searchText)
        // если был текст показываем результат
        if (searchText.isNotEmpty()) {
            historyLayout.isVisible = false // ИСПРАВЛЕНО ИМЯ
            recyclerView.isVisible = true
        } else if (historyTracks.isNotEmpty()) {
            historyLayout.isVisible = true // ИСПРАВЛЕНО ИМЯ
            recyclerView.isVisible = false
        }
    }

    // Метод для выполнения поиска треков
    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            // если пустой запрос, очищаем список и скрываем
            tracks.clear()
            (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
            hideAllPlaceholders()
            // показываем историю если есть что
            if (historyTracks.isNotEmpty()) {
                historyLayout.isVisible = true // ИСПРАВЛЕНО ИМЯ
                recyclerView.isVisible = false
            }
            return
        }

        tracks.clear() // Очищаем старые результаты перед новым поиском
        (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
        hideAllPlaceholders()
        recyclerView.isVisible = true

        val call = iTunesApiService.search(query)

        call.enqueue(object : Callback<ITunesResponse> {
            override fun onResponse(call: Call<ITunesResponse>, response: Response<ITunesResponse>) {
                if (response.isSuccessful) {
                    val iTunesResponse = response.body()
                    if (iTunesResponse != null && iTunesResponse.results.isNotEmpty()) {
                        tracks.addAll(iTunesResponse.results.map {
                            Track(
                                it.trackName ?: "Unknown Track",
                                it.artistName ?: "Unknown Artist",
                                SimpleDateFormat("mm:ss", Locale.getDefault()).format(it.trackTimeMillis),
                                it.artworkUrl100 ?: "",
                                it.trackId // Добавлено поле trackId, если оно есть в Track.kt
                            )
                        })
                        (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                        hideAllPlaceholders() // В случае успеха скрываем заглушки
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

    // Скрытие клавиатуры
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    companion object {
        const val SEARCH_TEXT_KEY = "searchText"
        const val TAG = "SearchActivity"

        private const val SEARCH_DEBOUNCE_DELAY = 2000L // константа для задержки дебаунса
    }
}