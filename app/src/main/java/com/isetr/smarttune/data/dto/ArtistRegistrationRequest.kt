package com.isetr.smarttune.data.dto

data class ArtistRegistrationRequest(
    val username: String,
    val nom: String,
    val prenom: String,
    val email: String,
    val numTel: String?,
    val dateNaissance: String,  // Format: "YYYY-MM-DD"
    val genre: String,  // "H" ou "F"
    val password: String,
    val bio: String
)
