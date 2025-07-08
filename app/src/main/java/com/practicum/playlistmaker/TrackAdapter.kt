package com.practicum.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private val tracks: ArrayList<Track>, // Список треков
    // обработчик кликов
    private val onTrackClickListener: (Track) -> Unit
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
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}