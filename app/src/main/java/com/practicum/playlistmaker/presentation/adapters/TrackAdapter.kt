package com.practicum.playlistmaker.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track

// логика отображения списка треков в recyclerview
class TrackAdapter(
    var tracks: ArrayList<Track>, // Список треков
    // обработчик кликов
    private val onTrackClickListener: (Track) -> Unit,
    private val onTrackLongClickListener: ((Track) -> Boolean)? = null
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track) // Вызывает bind для привязки данных

        // обработчик для элемента в списке
        holder.itemView.setOnClickListener {
            onTrackClickListener.invoke(track)
        }

        holder.itemView.setOnLongClickListener {
            onTrackLongClickListener?.invoke(track) ?: false
        }
    }

    override fun getItemCount(): Int = tracks.size
}