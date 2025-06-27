package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.appbar.MaterialToolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.inputmethod.EditorInfo
import android.util.Log // отладка
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale // ms в min:sec
import android.widget.Button
import android.widget.LinearLayout
import android.content.Context // для скрытия клавиатуры
import android.view.inputmethod.InputMethodManager // для управления клавиатурой

class SearchActivity : AppCompatActivity() {

    private lateinit var inputEditText: EditText
    private lateinit var clearIcon: ImageView

    // Сохранение текста в поиске
    private var searchText: String = ""

    private lateinit var recyclerView: RecyclerView // RecyclerView
    private val tracks = ArrayList<Track>() // список мок данных

    private lateinit var iTunesApiService: iTunesApi // Объявление переменной для API

    private lateinit var placeholderNoResults: LinearLayout // заглушка "нет результатов"
    private lateinit var placeholderServerError: LinearLayout // "ошибка сервера"
    private lateinit var refreshButton: Button // кнопа "обновить"

    companion object {
        // Ключ для текста в бандле
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
        private const val TAG = "SearchActivity"
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val toolbar = findViewById<MaterialToolbar>(R.id.title_search)
        inputEditText = findViewById(R.id.inputEditText)
        clearIcon = findViewById(R.id.clearIcon)

        placeholderNoResults = findViewById(R.id.placeholderNoResults) // иниш заглушек "нет результатов"
        placeholderServerError = findViewById(R.id.placeholderServerError) // "ошибка сервера"
        refreshButton = findViewById(R.id.refreshButton) // кнопа обновить

        // Кнопка назад в тулбаре
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (inputEditText.text.isNotEmpty()) {
                    performSearch() // Метод для выполнения поиска
                } else {
                    Log.d(TAG, "Поиск пустой, запрос не отправлен.")
                    // Очищаем список и скрываем его, если пустое
                    tracks.clear()
                    (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                    recyclerView.isVisible = false // Скрываем RV, если ничего нет
                    hideAllPlaceholders() // скрываем заглушки при пустом запросе
                }
                true // Возвращаем true, если отработано
            }
            false // иначе false
        }

        // Слежка за текстом в EditText
        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Сохраняем введённый текст
                searchText = s.toString()

                // Показываем/скрываем иконку очистики по контексту
                clearIcon.isVisible = !s.isNullOrEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Очистка при нажатии на кнопку Х
        clearIcon.setOnClickListener {
            inputEditText.text.clear()
            // Скрываем клавиатуру
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(inputEditText.windowToken, 0)

            tracks.clear()
            (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
            recyclerView.isVisible = false
            hideAllPlaceholders() // скрытие заглушек при очистке
        }

        refreshButton.setOnClickListener {
            performSearch()
        }

        // Обработка RecyclerView
        recyclerView = findViewById(R.id.recyclerView) // Инициализация вашего RecyclerView по ID

        val trackAdapter = TrackAdapter(tracks)

        // Устанавливаем LinearLayoutManager для RecyclerView.
        // Он будет располагать элементы списка в вертикальном порядке.
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Привязываем адаптер к RecyclerView
        recyclerView.adapter = trackAdapter

        // Создаем Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create()) // конвертер GSON
            .build()

        // Создаем экземпляр API из интерфейса iTunesApi
        iTunesApiService = retrofit.create(iTunesApi::class.java)
    }


    // Сохраняем текст из поля ввода перед уничтожением активности
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    // Восстанавливаем текст в EditText после пересоздания активности
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        inputEditText.setText(restoredText)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun performSearch() {
        val query = inputEditText.text.toString()
        if (query.isEmpty()) {
            return
        }

        recyclerView.isVisible = false
        hideAllPlaceholders()

        iTunesApiService.search(query).enqueue(object : Callback<ITunesResponse> {
            override fun onResponse(
                call: Call<ITunesResponse>,
                response: Response<ITunesResponse>
            ) {
                if (response.code() == 200) { // Проверяем что всё в порядке (ответ 200 - ок)
                    val iTunesResponse = response.body()
                    if (iTunesResponse != null && iTunesResponse.results.isNotEmpty()) {
                        // Если ответ успешный и с результатом, то
                        tracks.clear() // очищаем старые результаты перед добавлением новых
                        for (trackDto in iTunesResponse.results) {
                            // Форматируем длительность в мин:сек"
                            val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault())
                                .format(trackDto.trackTimeMillis)
                            tracks.add(
                                Track(
                                    trackName = trackDto.trackName
                                        ?: "", // Если используются значения null, то остаётся пустая строка.
                                    artistName = trackDto.artistName
                                        ?: "",
                                    trackTime = formattedTime,
                                    artworkUrl100 = trackDto.artworkUrl100
                                        ?: "" // пустышка для плейсхолдера
                                )
                            )
                        }
                        (recyclerView.adapter as TrackAdapter).notifyDataSetChanged() // Уведомляем адаптер
                        recyclerView.isVisible = true // Показываем RV
                        hideAllPlaceholders()
                        Log.d(TAG, "Поиск успешен, найдено ${tracks.size} треков.")
                    } else {
                        // Если ответ успешный и список пуст
                        tracks.clear()
                        (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                        recyclerView.isVisible = false // Скрываем RV
                        placeholderNoResults.isVisible = true // нет результатов
                        placeholderServerError.isVisible = false // ошибка сервера
                        Log.d(TAG, "Нет результатов по запросу: $query")
                    }
                } else {
                    // Если код ответа не 200, ошибка сервера
                    tracks.clear()
                    (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                    recyclerView.isVisible = false // Скрываем RV
                    placeholderServerError.isVisible = true
                    placeholderNoResults.isVisible = false
                    Log.e(TAG, "Ошибка сервера: ${response.code()}")
                }
            }

            // Метод при проблемах с интернетом
            override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                tracks.clear()
                (recyclerView.adapter as TrackAdapter).notifyDataSetChanged()
                recyclerView.isVisible = false // Скрываем RV
                placeholderServerError.isVisible = true
                placeholderNoResults.isVisible = false
                Log.e(
                    TAG,
                    "Ошибка запроса: ${t.message}",
                    t
                ) // Логируем ошибку
            }
        })
    }
    private fun hideAllPlaceholders() {
        placeholderNoResults.isVisible = false
        placeholderServerError.isVisible = false
    }

}

