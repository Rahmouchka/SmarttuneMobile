package com.isetr.smarttune.data

import com.isetr.smarttune.data.dto.ChansonSimple
import retrofit2.Response
import retrofit2.http.*

interface FavorisApi {

    @GET("api/user/{userId}/favoris")
    suspend fun getFavoris(@Path("userId") userId: Long): Response<List<ChansonSimple>>

    @POST("api/user/{userId}/favoris")
    suspend fun addToFavoris(
        @Path("userId") userId: Long,
        @Query("chansonId") chansonId: Long
    ): Response<String>

    @DELETE("api/user/{userId}/favoris/{chansonId}")
    suspend fun removeFromFavoris(
        @Path("userId") userId: Long,
        @Path("chansonId") chansonId: Long
    ): Response<String>
}

