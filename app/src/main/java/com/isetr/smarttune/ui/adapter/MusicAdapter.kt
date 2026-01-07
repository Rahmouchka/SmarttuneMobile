package com.isetr.smarttune.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.isetr.smarttune.R

data class Music(
    val id: Long,
    val title: String,
    val artistName: String,
    val duration: String,
    val playsCount: String,
    val albumArtUrl: String? = null
)

class MusicAdapter(
    private val musicList: List<Music>,
    private val onPlayClick: (Music) -> Unit,
    private val onMoreClick: (Music) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAlbumArt: ImageView = itemView.findViewById(R.id.iv_album_art)
        val tvSongTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvArtistName: TextView = itemView.findViewById(R.id.tvArtist)
        val tvPlays: TextView = itemView.findViewById(R.id.tv_plays)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        val btnPlay: ImageView = itemView.findViewById(R.id.btnPlay)
        val ivMore: ImageView = itemView.findViewById(R.id.iv_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music_card, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]

        holder.tvSongTitle.text = music.title
        holder.tvArtistName.text = music.artistName
        holder.tvPlays.text = music.playsCount
        holder.tvDuration.text = music.duration

        // TODO: Charger l'image avec Glide ou Coil
        // Glide.with(holder.itemView.context)
        //     .load(music.albumArtUrl)
        //     .placeholder(R.drawable.placeholder_album)
        //     .into(holder.ivAlbumArt)

        holder.btnPlay.setOnClickListener {
            onPlayClick(music)
        }

        holder.ivMore.setOnClickListener {
            onMoreClick(music)
        }
    }

    override fun getItemCount(): Int = musicList.size
}

// Utilisation dans l'Activity:
// val adapter = MusicAdapter(
//     musicList = listOf(...),
//     onPlayClick = { music -> /* Jouer la musique */ },
//     onMoreClick = { music -> /* Afficher les options */ }
// )
// binding.rvMusic.adapter = adapter