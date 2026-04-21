package com.practicum.playlistmaker.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Playlist

// Адаптер списка в шторке
class BottomSheetPlaylistAdapter(
    var playlists: List<Playlist>,
    private val itemClickListener: (Playlist) -> Unit
) : RecyclerView.Adapter<BottomSheetPlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_bottom_sheet, parent, false)
        return BottomSheetPlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: BottomSheetPlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener { itemClickListener.invoke(playlist) }
    }

    override fun getItemCount(): Int = playlists.size
}