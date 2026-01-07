package com.isetr.smarttune.data.dto

data class PlaylistResponse(
    val id: Long,
    val titre: String,
    val dateCreation: String?,
    val visible: Boolean,
    val createurId: Long?,
    val chansons: List<ChansonSimple>?
)

