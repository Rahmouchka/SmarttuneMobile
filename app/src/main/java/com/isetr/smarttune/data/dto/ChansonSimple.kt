package com.isetr.smarttune.data.dto

data class ChansonSimple(
    val id: Long,
    val titre: String,
    val url: String?,
    val duree: String?,
    val musicGenre: MusicGenre?
)

