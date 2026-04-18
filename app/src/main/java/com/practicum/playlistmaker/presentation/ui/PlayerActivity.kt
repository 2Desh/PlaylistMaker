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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.content.IntentCompat

class PlayerActivity : AppCompatActivity() {
    private val viewModel by viewModel<PlayerViewModel>()

    private lateinit var playButton: ImageView
    private lateinit var trackTimeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_audioplayer)

        initWindowInsets()
        initViews()

        val track = IntentCompat.getParcelableExtra(intent, TRACK_KEY, Track::class.java)
        if (track != null) {
            bindTrackData(track)

            val url = track.previewUrl
            if (!url.isNullOrEmpty()) {
                viewModel.preparePlayer(url)
            }
        }

        viewModel.stateLiveData.observe(this) { state ->
            renderState(state)
        }
    }

    private fun initWindowInsets() {
        val rootView = findViewById<View>(R.id.audioplayer)
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
                insets
            }
        }
    }

    private fun initViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.playerToolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        playButton = findViewById(R.id.playPauseButton)
        trackTimeTextView = findViewById(R.id.playbackProgressTextView)

        playButton.setOnClickListener {
            viewModel.playbackControl()
        }
    }

    private fun bindTrackData(track: Track) {
        // Основная информация
        findViewById<TextView>(R.id.trackNameTextView).text = track.trackName
        findViewById<TextView>(R.id.artistNameTextView).text = track.artistName

        // Форматируем время
        val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTime)
        findViewById<TextView>(R.id.durationValueTextView).text = formattedTime
        findViewById<TextView>(R.id.collectionNameValueTextView).text = track.collectionName

        val year = track.releaseDate?.take(4) ?: ""
        findViewById<TextView>(R.id.releaseDateValueTextView).text = year

        findViewById<TextView>(R.id.primaryGenreNameValueTextView).text = track.primaryGenreName
        findViewById<TextView>(R.id.countryValueTextView).text = track.country

        // Обложка трека
        val coverImageView = findViewById<ImageView>(R.id.artworkImageView)
        val artworkUrl512 = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.player_artwork_corner_radius)))
            .into(coverImageView)
    }

    private fun renderState(state: PlayerState) {
        when (state) {
            is PlayerState.Default -> {
                playButton.isEnabled = false
                playButton.setImageResource(R.drawable.ic_play)
                trackTimeTextView.text = state.progress
            }
            is PlayerState.Prepared -> {
                playButton.isEnabled = true
                playButton.setImageResource(R.drawable.ic_play)
                trackTimeTextView.text = state.progress
            }
            is PlayerState.Playing -> {
                playButton.setImageResource(R.drawable.ic_pause)
                trackTimeTextView.text = state.progress
            }
            is PlayerState.Paused -> {
                playButton.setImageResource(R.drawable.ic_play)
                trackTimeTextView.text = state.progress
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    companion object {
        const val TRACK_KEY = "track_key"
    }
}