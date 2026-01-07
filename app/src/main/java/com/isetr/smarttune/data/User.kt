package com.isetr.smarttune.data

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val nom: String?,
    val prenom: String?,
    val role: String, // "USER" ou "ARTIST"
    val isActive: Boolean = true,
    @SerializedName("user_type") val userType: String? = null // pour distinguer ARTIST
)
