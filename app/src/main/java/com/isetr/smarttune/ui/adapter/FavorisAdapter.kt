package com.isetr.smarttune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isetr.smarttune.R
import com.isetr.smarttune.data.dto.ChansonSimple
import com.isetr.smarttune.databinding.ItemMusicCardBinding

class FavorisAdapter(
    private val favoris: MutableList<ChansonSimple> = mutableListOf(),
    private val onPlayClick: (ChansonSimple) -> Unit = {},
    private val onPauseClick: (ChansonSimple) -> Unit = {},
    private val onRemoveClick: (ChansonSimple) -> Unit = {}
) : RecyclerView.Adapter<FavorisAdapter.FavorisViewHolder>() {

    private var currentPlayingId: Long? = null
    private var isCurrentlyPlaying: Boolean = false

    inner class FavorisViewHolder(private val binding: ItemMusicCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chanson: ChansonSimple, isPlaying: Boolean, isPaused: Boolean) {
            binding.tvTitle.text = chanson.titre
            binding.tvArtist.text = "Favori"

            // Afficher le cœur rempli (car c'est un favori)
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
            binding.btnFavorite.setColorFilter(0xFFFF6B6B.toInt())

            // Mettre à jour l'icône du bouton play
            updatePlayButtonIcon(isPlaying, isPaused)

            // Bouton play/pause
            binding.btnPlay.setOnClickListener {
                if (isPlaying && !isPaused) {
                    onPauseClick(chanson)
                } else {
                    currentPlayingId = chanson.id
                    isCurrentlyPlaying = true
                    notifyDataSetChanged()
                    onPlayClick(chanson)
                }
            }

            // Clic sur le cœur = retirer des favoris
            binding.btnFavorite.setOnClickListener {
                onRemoveClick(chanson)
            }

            // Bouton more = retirer des favoris aussi
            binding.ivMore.setOnClickListener {
                onRemoveClick(chanson)
            }
        }

        private fun updatePlayButtonIcon(isPlaying: Boolean, isPaused: Boolean) {
            when {
                isPlaying && !isPaused -> {
                    binding.btnPlay.setImageResource(android.R.drawable.ic_media_pause)
                    binding.btnPlay.alpha = 1.0f
                }
                isPlaying && isPaused -> {
                    binding.btnPlay.setImageResource(android.R.drawable.ic_media_play)
                    binding.btnPlay.alpha = 0.7f
                }
                else -> {
                    binding.btnPlay.setImageResource(android.R.drawable.ic_media_play)
                    binding.btnPlay.alpha = 1.0f
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavorisViewHolder {
        val binding = ItemMusicCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavorisViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavorisViewHolder, position: Int) {
        val chanson = favoris[position]
        val isPlaying = chanson.id == currentPlayingId
        val isPaused = isPlaying && !isCurrentlyPlaying
        holder.bind(chanson, isPlaying, isPaused)
    }

    override fun getItemCount() = favoris.size

    fun setFavoris(newFavoris: List<ChansonSimple>) {
        favoris.clear()
        favoris.addAll(newFavoris)
        notifyDataSetChanged()
    }

    fun updatePlayingState(chansonId: Long?, isPlaying: Boolean) {
        currentPlayingId = chansonId
        isCurrentlyPlaying = isPlaying
        notifyDataSetChanged()
    }
}

