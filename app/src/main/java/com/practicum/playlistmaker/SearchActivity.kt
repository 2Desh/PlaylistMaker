package com.practicum.playlistmaker

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
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.content.Intent

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

    private lateinit var progressBar: ProgressBar

    private lateinit var searchHistory: SearchHistory

    private val historyTracks = ArrayList<Track>()              // список треков для истории поиска
    // !!! АДАПТЕР ДЛЯ ИСТОРИИ ПОИСКА !!!
    private lateinit var historyAdapter: TrackAdapter

    private var isClickAllowed = true

    // Handler для дебаунса
    private val handler = Handler(Looper.getMainLooper())
    // Runnable для дебаунса
    private val searchRunnable = Runnable { performSearch(inputEditText.text.toString()) }


    override fun onCreate(savedInstanceState: Bundle?) { // TODO: Очень перегруженный. Сделай рефакторинг. Вынеси инициализацию View и слушателей в отдельные методы.
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
        progressBar = findViewById(R.id.progressBar)

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
            if (clickDebounce()) {
                searchHistory.addTrackToHistory(track)
                historyTracks.clear()
                historyTracks.addAll(searchHistory.getHistory())
                historyAdapter.notifyDataSetChanged()

                openPlayer(track) // переход на экран плеера
            }
        }

        // адаптер для истории
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(historyTracks) { track ->
            if (clickDebounce()) {
                // добавляем трек в историю
                searchHistory.addTrackToHistory(track)
                historyTracks.clear()
                historyTracks.addAll(searchHistory.getHistory())
                historyAdapter.notifyDataSetChanged()

                openPlayer(track) // переход на экран плеера

                // поиск по имени
                // inputEditText.setText(track.trackName)
                // performSearch(track.trackName)
            }
        }
        historyRecyclerView.adapter = historyAdapter


        // обработчик кнопки очистки поля ввода
        clearIcon.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()

            handler.removeCallbacks(searchRunnable)

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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {} // вызывается до изменения текста, позволяет узнать старое значение

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearIcon.isVisible = !s.isNullOrEmpty()
                searchText = s.toString()
                hideAllPlaceholders() // скрываем все заглушки

                // логика отображения или скрытия истории и результатов поиска
                if (s.isNullOrEmpty() && historyTracks.isNotEmpty()) {
                    historyLayout.isVisible = true // ИСПРАВЛЕНО ИМЯ
                    recyclerView.isVisible = false // скрываем результаты поиска
                    // Если текст стерли, отменяем запланированный поиск
                    handler.removeCallbacks(searchRunnable)
                } else {
                    historyLayout.isVisible = false
                    // Запускаем дебаунс поиска
                    searchDebounce()
                }
            }
            override fun afterTextChanged(s: Editable?) {} // обязательный метод интерфейса TextWatcher, сейчас не используется
        }

        inputEditText.addTextChangedListener(simpleTextWatcher)

        // обработчик для поля ввода
        inputEditText.setOnFocusChangeListener { _, hasFocus ->
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

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
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

        recyclerView.isVisible = false
        progressBar.isVisible = true

        val call = iTunesApiService.search(query)

        call.enqueue(object : Callback<ITunesResponse> {
            override fun onResponse(call: Call<ITunesResponse>, response: Response<ITunesResponse>) {
                progressBar.isVisible = false
                if (response.isSuccessful) {
                    val iTunesResponse = response.body()
                    if (iTunesResponse != null && iTunesResponse.results.isNotEmpty()) {
                        tracks.addAll(iTunesResponse.results.map {
                            Track(
                                trackName = it.trackName ?: "Unknown Track",
                                artistName = it.artistName ?: "Unknown Artist",
                                trackTime = it.trackTimeMillis ?: 0L,
                                artworkUrl100 = it.artworkUrl100 ?: "",
                                trackId = it.trackId ?: 0L,
                                collectionName = it.collectionName,
                                releaseDate = it.releaseDate,
                                primaryGenreName = it.primaryGenreName,
                                country = it.country,
                                previewUrl = it.previewUrl
                            )
                        })

                        recyclerView.isVisible = true

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
                progressBar.isVisible = false
                tracks.clear()
                (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                recyclerView.isVisible = false
                placeholderServerError.isVisible = true
                placeholderNoResults.isVisible = false
                Log.e(TAG, "Ошибка запроса: ${t.message}", t)
            }
        })
    }

    private fun clickDebounce() : Boolean {
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

    // Скрытие клавиатуры
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    companion object {
        const val SEARCH_TEXT_KEY = "searchText"
        const val TAG = "SearchActivity"

        private const val SEARCH_DEBOUNCE_DELAY = 2000L // константы для задержки дебаунса
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}