package com.isetr.smarttune.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey
    val id: Int = 1, // Une seule session actuelle
    val userId: Long,
    val userRole: String // "USER", "ARTIST", "ADMIN"
)

