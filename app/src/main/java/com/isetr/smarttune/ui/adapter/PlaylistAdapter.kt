package com.isetr.smarttune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isetr.smarttune.data.dto.PlaylistResponse
import com.isetr.smarttune.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val playlists: MutableList<PlaylistResponse> = mutableListOf(),
    private val onPlaylistClick: (PlaylistResponse) -> Unit = {},
    private val onMoreClick: (PlaylistResponse) -> Unit = {}
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: PlaylistResponse) {
            binding.tvPlaylistTitle.text = playlist.titre

            val count = playlist.chansons?.size ?: 0
            binding.tvPlaylistCount.text = "$count chanson${if (count > 1) "s" else ""}"

            binding.root.setOnClickListener {
                onPlaylistClick(playlist)
            }

            binding.btnMore.setOnClickListener {
                onMoreClick(playlist)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount() = playlists.size

    fun setPlaylists(newPlaylists: List<PlaylistResponse>) {
        playlists.clear()
        playlists.addAll(newPlaylists)
        notifyDataSetChanged()
    }
}

