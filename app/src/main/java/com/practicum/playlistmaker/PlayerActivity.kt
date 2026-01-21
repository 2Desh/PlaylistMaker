package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class PlayerActivity : AppCompatActivity() {

    // UI элементы
    private lateinit var artworkImageView: ImageView
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var playbackProgressTextView: TextView
    private lateinit var collectionNameValueTextView: TextView
    private lateinit var releaseDateValueTextView: TextView
    private lateinit var primaryGenreNameValueTextView: TextView
    private lateinit var countryValueTextView: TextView
    private lateinit var playPauseButton: ImageView

    // Логика плеера
    private var mediaPlayer = MediaPlayer()
    private var playerState = STATE_DEFAULT
    private var previewUrl: String? = null

    // Handler для обновления времени
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    // Задача для обновления таймера
    private val updateTimerTask = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                // Обновляем текущее время воспроизведения
                playbackProgressTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition)
                // Запускаем снова через 300 мс (достаточно плавно для секунд)
                mainThreadHandler.postDelayed(this, DELAY)
            }
        }
    }

    companion object {
        const val TRACK_KEY = "track_key"

        // Константы состояний
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3

        private const val DELAY = 300L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer) // Убедись, что имя файла совпадает

        val rootView = findViewById<View>(R.id.audioplayer)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        initViews()

        // Настройка Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.playerToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Получение данных трека
        val track = intent.getParcelableExtra<Track>(TRACK_KEY)

        if (track != null) {
            bindTrackData(track)
            previewUrl = track.previewUrl
            preparePlayer()
        } else {
            finish()
        }

        // Обработка нажатия на кнопку Play/Pause
        playPauseButton.setOnClickListener {
            playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer() // Ставим на паузу при сворачивании
    }

    override fun onDestroy() {
        super.onDestroy()
        mainThreadHandler.removeCallbacks(updateTimerTask) // Удаляем колбэк таймера
        mediaPlayer.release() // Освобождаем ресурсы плеера
    }

    private fun initViews() {
        artworkImageView = findViewById(R.id.artworkImageView)
        trackNameTextView = findViewById(R.id.trackNameTextView)
        artistNameTextView = findViewById(R.id.artistNameTextView)
        playbackProgressTextView = findViewById(R.id.playbackProgressTextView)
        collectionNameValueTextView = findViewById(R.id.collectionNameValueTextView)
        releaseDateValueTextView = findViewById(R.id.releaseDateValueTextView)
        primaryGenreNameValueTextView = findViewById(R.id.primaryGenreNameValueTextView)
        countryValueTextView = findViewById(R.id.countryValueTextView)
        playPauseButton = findViewById(R.id.playPauseButton)
    }

    @SuppressLint("SetTextI18n")
    private fun bindTrackData(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName

        // Важно: начальное значение таймера 00:00
        playbackProgressTextView.text = "00:00"

        val cornerRadius = resources.getDimensionPixelSize(R.dimen.player_artwork_corner_radius)
        Glide.with(this)
            .load(track.getCoverArtwork())
            .centerCrop()
            .apply(RequestOptions.bitmapTransform(RoundedCorners(cornerRadius)))
            .placeholder(R.drawable.placeholder)
            .into(artworkImageView)

        collectionNameValueTextView.text = track.collectionName ?: ""
        releaseDateValueTextView.text = track.releaseDate?.take(4) ?: ""
        primaryGenreNameValueTextView.text = track.primaryGenreName ?: ""
        countryValueTextView.text = track.country ?: ""

        // Если нужно отобразить полную длительность (не превью) в таблице, можно добавить это здесь
        // Но для progress bar мы используем логику 00:00 -> 00:30
    }

    private fun preparePlayer() {
        if (previewUrl == null) return

        try {
            mediaPlayer.setDataSource(previewUrl)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                playPauseButton.isEnabled = true
                playerState = STATE_PREPARED
            }
            mediaPlayer.setOnCompletionListener {
                // Когда трек заканчивается (30 сек)
                playPauseButton.setImageResource(R.drawable.ic_play)
                playerState = STATE_PREPARED
                mainThreadHandler.removeCallbacks(updateTimerTask)
                playbackProgressTextView.text = "00:00" // Сбрасываем таймер в 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playPauseButton.setImageResource(R.drawable.ic_pause) // Меняем на паузу
        playerState = STATE_PLAYING
        mainThreadHandler.post(updateTimerTask)
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playPauseButton.setImageResource(R.drawable.ic_play) // Меняем на play
        playerState = STATE_PAUSED
        mainThreadHandler.removeCallbacks(updateTimerTask)
    }

    private fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }
}