package com.isetr.smarttune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isetr.smarttune.data.dto.ChansonSimple
import com.isetr.smarttune.databinding.ItemPlaylistSongBinding

class PlaylistSongAdapter(
    private val songs: MutableList<ChansonSimple> = mutableListOf(),
    private val onPlayClick: (ChansonSimple) -> Unit = {},
    private val onRemoveClick: (ChansonSimple) -> Unit = {}
) : RecyclerView.Adapter<PlaylistSongAdapter.SongViewHolder>() {

    inner class SongViewHolder(private val binding: ItemPlaylistSongBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chanson: ChansonSimple) {
            binding.tvSongTitle.text = chanson.titre
            binding.tvSongGenre.text = chanson.musicGenre?.name ?: "Musique"

            binding.btnPlay.setOnClickListener {
                onPlayClick(chanson)
            }

            binding.btnRemove.setOnClickListener {
                onRemoveClick(chanson)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemPlaylistSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount() = songs.size

    fun setSongs(newSongs: List<ChansonSimple>) {
        songs.clear()
        songs.addAll(newSongs)
        notifyDataSetChanged()
    }
}

