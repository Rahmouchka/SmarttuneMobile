package com.isetr.smarttune.data.dto

data class ArtistRequest(
    val id: Long?,
    val username: String,
    val email: String,
    val nom: String,
    val prenom: String,
    val age: Int,
    val genre: String,
    val numTel: String?,
    val bio: String,
    val status: String = "PENDING"
)
