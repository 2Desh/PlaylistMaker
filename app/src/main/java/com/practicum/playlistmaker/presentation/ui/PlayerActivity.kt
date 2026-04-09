package com.practicum.playlistmaker.presentation.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.Creator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

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

    // логика плеера через новую архитектуру
    private val audioPlayerInteractor = Creator.provideAudioPlayerInteractor()
    private var playerState = STATE_DEFAULT
    private var previewUrl: String? = null

    // handler для обновления времени
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    // задача для обновления таймера
    @Suppress("kotlin:S6516")
    private val updateTimerTask = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                // обновляем текущее время воспроизведения через интерактор
                playbackProgressTextView.text = dateFormat.format(audioPlayerInteractor.getCurrentPosition())
                mainThreadHandler.postDelayed(this, DELAY)
            }
        }
    }

    private val dateFormat by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

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

        // настройка toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.playerToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // получение данных трека
        @Suppress("DEPRECATION")
        val track = intent.getParcelableExtra<Track>(TRACK_KEY)

        if (track != null) {
            bindTrackData(track)
            previewUrl = track.previewUrl
            preparePlayer()
        } else {
            finish()
        }

        // обработка нажатия на кнопку play/pause
        playPauseButton.setOnClickListener {
            playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer() // ставим на паузу при сворачивании
    }

    override fun onDestroy() {
        super.onDestroy()
        mainThreadHandler.removeCallbacks(updateTimerTask) // удаляем колбэк таймера
        audioPlayerInteractor.release() // освобождаем ресурсы плеера через интерактор
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

        playbackProgressTextView.text = dateFormat.format(0)

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
    }

    private fun preparePlayer() {
        if (previewUrl == null) return

        // передаем ссылку и два колбэка в интерактор
        audioPlayerInteractor.preparePlayer(
            url = previewUrl!!,
            onPreparedListener = {
                playPauseButton.isEnabled = true
                playerState = STATE_PREPARED
            },
            onCompletionListener = {
                // когда трек заканчивается
                playPauseButton.setImageResource(R.drawable.ic_play)
                playerState = STATE_PREPARED
                mainThreadHandler.removeCallbacks(updateTimerTask)
                playbackProgressTextView.text = dateFormat.format(0) // сбрасываем таймер в 0
            }
        )
    }

    private fun startPlayer() {
        audioPlayerInteractor.startPlayer()
        playPauseButton.setImageResource(R.drawable.ic_pause) // меняем на паузу
        playerState = STATE_PLAYING
        mainThreadHandler.post(updateTimerTask)
    }

    private fun pausePlayer() {
        audioPlayerInteractor.pausePlayer()
        playPauseButton.setImageResource(R.drawable.ic_play) // меняем на play
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

    companion object {
        const val TRACK_KEY = "track_key"

        // константы состояний
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3

        private const val DELAY = 300L
    }
}