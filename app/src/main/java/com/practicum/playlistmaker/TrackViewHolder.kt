package com.practicum.playlistmaker

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)
    private val trackNameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artistNameTextView)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.trackTimeTextView)

    // Метод для привязки данных объекта Track к view элементам
    fun bind(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        trackTimeTextView.text = track.trackTime

        // Закругление
        val cornerRadius = itemView.resources.getDimensionPixelSize(R.dimen.cover_corner_radius)

        // RequestOptions для Glide, чтобы закруглить углы
        val requestOptions = RequestOptions().transform(RoundedCorners(cornerRadius))

        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .apply(requestOptions) // Применяется закругление
            .placeholder(R.drawable.placeholder) // Заглушка если нет инета
            .centerCrop() // Маштаб
            .into(artworkImageView)
    }
}
