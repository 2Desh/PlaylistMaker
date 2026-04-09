package com.practicum.playlistmaker.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track

class PlayerActivity : AppCompatActivity() {

    private lateinit var artworkImageView: ImageView
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var playbackProgressTextView: TextView
    private lateinit var collectionNameValueTextView: TextView
    private lateinit var releaseDateValueTextView: TextView
    private lateinit var primaryGenreNameValueTextView: TextView
    private lateinit var countryValueTextView: TextView
    private lateinit var playPauseButton: ImageView

    // Объявляем ViewModel
    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        // Инициализируем ViewModel с помощью нашей Фабрики
        viewModel = ViewModelProvider(this, PlayerViewModelFactory())[PlayerViewModel::class.java]

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

        val toolbar = findViewById<MaterialToolbar>(R.id.playerToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Подписываемся на изменения состояния из ViewModel.
        // Каждый раз, когда ViewModel меняет состояние, вызывается метод render()
        viewModel.stateLiveData.observe(this) { state ->
            render(state)
        }

        @Suppress("DEPRECATION")
        val track = intent.getParcelableExtra<Track>(TRACK_KEY)

        if (track != null) {
            bindTrackData(track)

            // Если ссылка на превью есть, передаем её во ViewModel для подготовки плеера
            track.previewUrl?.let { url ->
                viewModel.preparePlayer(url)
            }
        } else {
            finish()
        }

        // При нажатии на кнопку просто делегируем задачу во ViewModel
        playPauseButton.setOnClickListener {
            viewModel.playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        // При сворачивании приложения ставим плеер на паузу через ViewModel
        viewModel.pausePlayer()
    }

    // Метод onDestroy() мы удалили, так как ViewModel.onCleared()
    // сама всё корректно очистит при закрытии Activity.

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

    private fun bindTrackData(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName

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

    // Этот метод отвечает исключительно за отрисовку UI на основе текущего состояния
    private fun render(state: PlayerState) {
        playPauseButton.isEnabled = state.isPlayButtonEnabled
        playbackProgressTextView.text = state.progress

        when (state.buttonText) {
            "PLAY" -> playPauseButton.setImageResource(R.drawable.ic_play)
            "PAUSE" -> playPauseButton.setImageResource(R.drawable.ic_pause)
        }
    }

    companion object {
        const val TRACK_KEY = "track_key"
    }
}