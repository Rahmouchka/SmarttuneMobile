package com.isetr.smarttune.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.isetr.smarttune.R
import com.isetr.smarttune.data.dto.ChansonResponse
import com.isetr.smarttune.databinding.ItemMusicCardBinding

class ChansonAdapter(
    private val chansons: MutableList<ChansonResponse> = mutableListOf(),
    private val onPlayClick: (ChansonResponse) -> Unit = {},
    private val onPauseClick: (ChansonResponse) -> Unit = {},
    private val onFavoriteClick: (ChansonResponse) -> Unit = {},
    private val onAddToPlaylistClick: (ChansonResponse) -> Unit = {},
) : RecyclerView.Adapter<ChansonAdapter.ChansonViewHolder>() {

    private var currentPlayingId: Long? = null
    private var isCurrentlyPlaying: Boolean = false
    private var favoriteIds: Set<Long> = emptySet()

    inner class ChansonViewHolder(private val binding: ItemMusicCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chanson: ChansonResponse, isPlaying: Boolean, isPaused: Boolean, isFavorite: Boolean) {
            binding.tvTitle.text = chanson.titre

            // Afficher "Single" si pas d'album
            binding.tvArtist.text = chanson.albumTitre ?: "Single"

            // Mettre à jour l'icône du bouton play selon l'état
            updatePlayButtonIcon(binding, chanson.id, isPlaying, isPaused)

            // Mettre à jour l'icône du bouton favoris
            updateFavoriteIcon(binding, isFavorite)

            // Bouton play/pause
            binding.btnPlay.setOnClickListener {
                android.util.Log.d("ChansonAdapter", "Play clicked for: ${chanson.titre} (ID: ${chanson.id})")

                if (isPlaying && !isPaused) {
                    // En cours de lecture → Pause
                    android.util.Log.d("ChansonAdapter", "Currently playing, pausing...")
                    onPauseClick(chanson)
                } else {
                    // Pas en lecture ou en pause → Play
                    android.util.Log.d("ChansonAdapter", "Not playing or paused, playing...")
                    currentPlayingId = chanson.id
                    isCurrentlyPlaying = true
                    notifyDataSetChanged()
                    onPlayClick(chanson)
                }
            }

            // Bouton favoris
            binding.btnFavorite.setOnClickListener {
                onFavoriteClick(chanson)
            }

            // Bouton ajouter à playlist
            binding.btnAddToPlaylist.setOnClickListener {
                onAddToPlaylistClick(chanson)
            }
        }

        private fun updatePlayButtonIcon(
            binding: ItemMusicCardBinding,
            chansonId: Long,
            isPlaying: Boolean,
            isPaused: Boolean
        ) {
            when {
                isPlaying && !isPaused -> {
                    // En cours de lecture → afficher icône pause
                    binding.btnPlay.setImageResource(android.R.drawable.ic_media_pause)
                    binding.btnPlay.alpha = 1.0f
                    android.util.Log.d("ChansonAdapter", "Icon: PAUSE")
                }
                isPlaying && isPaused -> {
                    // En pause → afficher icône play
                    binding.btnPlay.setImageResource(android.R.drawable.ic_media_play)
                    binding.btnPlay.alpha = 0.7f
                    android.util.Log.d("ChansonAdapter", "Icon: PLAY (paused)")
                }
                else -> {
                    // Pas en lecture → afficher icône play
                    binding.btnPlay.setImageResource(android.R.drawable.ic_media_play)
                    binding.btnPlay.alpha = 1.0f
                    android.util.Log.d("ChansonAdapter", "Icon: PLAY (stopped)")
                }
            }
        }

        private fun updateFavoriteIcon(binding: ItemMusicCardBinding, isFavorite: Boolean) {
            if (isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
                binding.btnFavorite.setColorFilter(0xFFFF6B6B.toInt())
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
                binding.btnFavorite.setColorFilter(0xFF888888.toInt())
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
        val chanson = chansons[position]
        val isPlaying = chanson.id == currentPlayingId
        val isPaused = isPlaying && !isCurrentlyPlaying
        val isFavorite = favoriteIds.contains(chanson.id)
        holder.bind(chanson, isPlaying, isPaused, isFavorite)
    }

    override fun getItemCount() = chansons.size

    fun setChansons(newChansons: List<ChansonResponse>) {
        chansons.clear()
        chansons.addAll(newChansons)
        notifyDataSetChanged()
    }

    fun updateFavorites(newFavoriteIds: Set<Long>) {
        favoriteIds = newFavoriteIds
        notifyDataSetChanged()
    }

    fun updatePlayingState(chansonId: Long?, isPlaying: Boolean) {
        android.util.Log.d("ChansonAdapter", "updatePlayingState: chansonId=$chansonId, isPlaying=$isPlaying")
        currentPlayingId = chansonId
        isCurrentlyPlaying = isPlaying
        notifyDataSetChanged()
    }

    fun pauseCurrentSong() {
        android.util.Log.d("ChansonAdapter", "pauseCurrentSong: currentPlayingId=$currentPlayingId")
        isCurrentlyPlaying = false
        notifyDataSetChanged()
    }

    fun resumeCurrentSong() {
        android.util.Log.d("ChansonAdapter", "resumeCurrentSong: currentPlayingId=$currentPlayingId")
        isCurrentlyPlaying = true
        notifyDataSetChanged()
    }
}

