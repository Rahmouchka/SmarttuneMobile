package com.isetr.smarttune.data

import com.isetr.smarttune.data.dto.PlaylistResponse
import retrofit2.Response
import retrofit2.http.*

interface PlaylistApi {

    @GET("api/user/{userId}/playlists")
    suspend fun getPlaylists(@Path("userId") userId: Long): Response<List<PlaylistResponse>>

    @POST("api/user/{userId}/playlists")
    suspend fun createPlaylist(
        @Path("userId") userId: Long,
        @Query("titre") titre: String,
        @Query("visible") visible: Boolean = true
    ): Response<PlaylistResponse>

    @PUT("api/user/{userId}/playlists/{playlistId}")
    suspend fun updatePlaylist(
        @Path("userId") userId: Long,
        @Path("playlistId") playlistId: Long,
        @Query("titre") titre: String,
        @Query("visible") visible: Boolean
    ): Response<PlaylistResponse>

    @DELETE("api/user/{userId}/playlists/{playlistId}")
    suspend fun deletePlaylist(
        @Path("userId") userId: Long,
        @Path("playlistId") playlistId: Long
    ): Response<String>

    @POST("api/user/{userId}/playlists/{playlistId}/chansons")
    suspend fun addChansonsToPlaylist(
        @Path("userId") userId: Long,
        @Path("playlistId") playlistId: Long,
        @Body chansonIds: List<Long>
    ): Response<PlaylistResponse>

    @DELETE("api/user/{userId}/playlists/{playlistId}/chansons/{chansonId}")
    suspend fun removeChansonFromPlaylist(
        @Path("userId") userId: Long,
        @Path("playlistId") playlistId: Long,
        @Path("chansonId") chansonId: Long
    ): Response<PlaylistResponse>
}

