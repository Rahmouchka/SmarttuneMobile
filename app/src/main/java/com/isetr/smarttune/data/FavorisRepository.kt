package com.isetr.smarttune.data

import com.isetr.smarttune.data.dto.ChansonSimple

class FavorisRepository {

    private val api = RetrofitClient.getFavorisApi()

    suspend fun getFavoris(userId: Long): Result<List<ChansonSimple>> {
        return try {
            val response = api.getFavoris(userId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToFavoris(userId: Long, chansonId: Long): Result<Boolean> {
        return try {
            val response = api.addToFavoris(userId, chansonId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromFavoris(userId: Long, chansonId: Long): Result<Boolean> {
        return try {
            val response = api.removeFromFavoris(userId, chansonId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

