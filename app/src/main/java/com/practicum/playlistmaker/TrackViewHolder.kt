package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.util.concurrent.TimeUnit

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)
    private val trackNameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artistNameTextView)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.trackTimeTextView)

    @SuppressLint("DefaultLocale")
    fun bind(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName

        val minutes = TimeUnit.MILLISECONDS.toMinutes(track.trackTime)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(track.trackTime) % 60
        trackTimeTextView.text = String.format("%02d:%02d", minutes, seconds)

        val cornerRadius = itemView.resources.getDimensionPixelSize(R.dimen.cover_corner_radius)
        val requestOptions = RequestOptions().transform(RoundedCorners(cornerRadius))

        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .centerCrop()
            .apply(requestOptions)
            .placeholder(R.drawable.placeholder)
            .into(artworkImageView)
    }
}
