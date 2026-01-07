package com.isetr.smarttune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isetr.smarttune.data.dto.ChansonResponse
import com.isetr.smarttune.databinding.ItemMusicCardBinding

class ChansonAdapter(
    private val chansons: MutableList<ChansonResponse> = mutableListOf(),
    private val onPlayClick: (ChansonResponse) -> Unit = {}
) : RecyclerView.Adapter<ChansonAdapter.ChansonViewHolder>() {

    inner class ChansonViewHolder(private val binding: ItemMusicCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chanson: ChansonResponse) {
            binding.tvTitle.text = chanson.titre

            // Afficher "Single" si pas d'album
            binding.tvArtist.text = chanson.albumTitre ?: "Single"

            // Bouton play
            binding.btnPlay.setOnClickListener {
                onPlayClick(chanson)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChansonViewHolder {
        val binding = ItemMusicCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChansonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChansonViewHolder, position: Int) {
        holder.bind(chansons[position])
    }

    override fun getItemCount() = chansons.size

    fun setChansons(newChansons: List<ChansonResponse>) {
        chansons.clear()
        chansons.addAll(newChansons)
        notifyDataSetChanged()
    }
}

