package com.isetr.smarttune.data

import com.isetr.smarttune.data.dto.PlaylistResponse

class PlaylistRepository {

    private val api = RetrofitClient.getPlaylistApi()

    suspend fun getPlaylists(userId: Long): Result<List<PlaylistResponse>> {
        return try {
            val response = api.getPlaylists(userId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPlaylist(userId: Long, titre: String, visible: Boolean = true): Result<PlaylistResponse> {
        return try {
            val response = api.createPlaylist(userId, titre, visible)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePlaylist(userId: Long, playlistId: Long, titre: String, visible: Boolean): Result<PlaylistResponse> {
        return try {
            val response = api.updatePlaylist(userId, playlistId, titre, visible)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePlaylist(userId: Long, playlistId: Long): Result<Boolean> {
        return try {
            val response = api.deletePlaylist(userId, playlistId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addChansonsToPlaylist(userId: Long, playlistId: Long, chansonIds: List<Long>): Result<PlaylistResponse> {
        return try {
            val response = api.addChansonsToPlaylist(userId, playlistId, chansonIds)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeChansonFromPlaylist(userId: Long, playlistId: Long, chansonId: Long): Result<PlaylistResponse> {
        return try {
            val response = api.removeChansonFromPlaylist(userId, playlistId, chansonId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

