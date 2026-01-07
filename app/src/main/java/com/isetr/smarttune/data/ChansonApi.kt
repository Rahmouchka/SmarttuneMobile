package com.isetr.smarttune.data

import com.isetr.smarttune.data.dto.ChansonResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChansonApi {

    @GET("api/search/chansons")
    suspend fun searchChansons(@Query("query") query: String): Response<List<ChansonResponse>>

    @GET("api/chansons/random")
    suspend fun getRandomChansons(@Query("humeur") humeur: String): Response<List<ChansonResponse>>

    @GET("api/chansons/{id}")
    suspend fun getChansonDetails(@Path("id") chansonId: Long): Response<ChansonResponse>
}

