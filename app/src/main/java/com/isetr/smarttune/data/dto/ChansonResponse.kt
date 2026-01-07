package com.isetr.smarttune.data.dto

enum class MusicGenre {
    POP, ROCK, RAP, CLASSICAL, JAZZ, ELECTRONIC, REGGAE, RNb, AUTRE
}

data class ChansonResponse(
    val id: Long,
    val titre: String,
    val url: String,
    val duree: String,
    val musicGenre: MusicGenre,
    val albumId: Long?,
    val albumTitre: String?
)

