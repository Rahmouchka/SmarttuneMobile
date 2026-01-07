package com.isetr.smarttune.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val username: String,
    val email: String,
    val password: String, // Stocké en clair - en production, utiliser du hashing
    val nom: String?,
    val prenom: String?,
    val role: String, // "USER" ou "ARTIST"
    val isActive: Boolean = true,
    val userType: String? = null // pour distinguer ARTIST
)

