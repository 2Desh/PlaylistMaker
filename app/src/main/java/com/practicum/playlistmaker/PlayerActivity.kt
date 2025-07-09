package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.MaterialToolbar
import java.util.concurrent.TimeUnit

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

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        // Инициализация View-элементов
        artworkImageView = findViewById(R.id.artworkImageView)
        trackNameTextView = findViewById(R.id.trackNameTextView)
        artistNameTextView = findViewById(R.id.artistNameTextView)
        playbackProgressTextView = findViewById(R.id.playbackProgressTextView)
        collectionNameValueTextView = findViewById(R.id.collectionNameValueTextView)
        releaseDateValueTextView = findViewById(R.id.releaseDateValueTextView)
        primaryGenreNameValueTextView = findViewById(R.id.primaryGenreNameValueTextView)
        countryValueTextView = findViewById(R.id.countryValueTextView)
        playPauseButton = findViewById(R.id.playPauseButton)

        // Обработка кнопки "назад" в MaterialToolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.playerToolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Получение Track из Intent
        val track = intent.getParcelableExtra<Track>(TRACK_KEY)

        track?.let {
            // Заполнение полей данными из объекта Track
            trackNameTextView.text = it.trackName
            artistNameTextView.text = it.artistName
            val minutes = TimeUnit.MILLISECONDS.toMinutes(it.trackTime)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(it.trackTime) % 60
            playbackProgressTextView.text = String.format("%02d:%02d", minutes, seconds)

            // Загрузка обложки с закругленными углами
            val cornerRadius = resources.getDimensionPixelSize(R.dimen.player_artwork_corner_radius)
            Glide.with(this)
                .load(it.getCoverArtwork())
                .centerCrop()
                .apply(RequestOptions.bitmapTransform(RoundedCorners(cornerRadius)))
                .placeholder(R.drawable.placeholder)
                .into(artworkImageView)

            // Заполнение таблицы с деталями трека
            collectionNameValueTextView.text = it.collectionName ?: ""
            releaseDateValueTextView.text = it.releaseDate?.take(4) ?: "" // Берем только год
            primaryGenreNameValueTextView.text = it.primaryGenreName ?: ""
            countryValueTextView.text = it.country ?: ""
        }

        playPauseButton.setOnClickListener {
            // Здесь будет логика для паузы/воспроизведения и смены иконки
        }
    }

    companion object {
        const val TRACK_KEY = "track_key"
    }
}