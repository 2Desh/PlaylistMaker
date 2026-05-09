package com.practicum.playlistmaker.presentation.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Playlist
import java.io.File

// Держатель элементов плейлиста
class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val coverImage: ImageView = itemView.findViewById(R.id.playlistCoverImageView)
    private val nameText: TextView = itemView.findViewById(R.id.playlistNameTextView)
    private val countText: TextView = itemView.findViewById(R.id.tracksCountTextView)

    fun bind(playlist: Playlist) {
        nameText.text = playlist.name

        // Вспомогательный метод для правильного склонения слова "трек"
        countText.text = itemView.resources.getQuantityString(R.plurals.tracks_count, playlist.trackCount, playlist.trackCount)

        // Загрузка обложки
        if (!playlist.coverFilePath.isNullOrEmpty()) {
            val file = File(playlist.coverFilePath)
            if (file.exists()) {
                Glide.with(itemView)
                    .load(file)
                    .transform(CenterCrop(), RoundedCorners(8))
                    .placeholder(R.drawable.placeholder)
                    .into(coverImage)
            } else {
                coverImage.setImageResource(R.drawable.placeholder)
            }
        } else {
            coverImage.setImageResource(R.drawable.placeholder)
        }
    }

}