package com.isetr.smarttune.data

import com.isetr.smarttune.data.dto.ArtistRequest
import com.isetr.smarttune.data.dto.LoginRequest
import com.isetr.smarttune.data.dto.UserRegistrationRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AuthApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<User>

    @POST("api/auth/register/user")
    suspend fun registerUser(@Body request: UserRegistrationRequest): Response<User>

    @Multipart
    @POST("api/auth/register/artist")
    suspend fun registerArtist(
        @Part("username") username: RequestBody,
        @Part("nom") nom: RequestBody,
        @Part("prenom") prenom: RequestBody,
        @Part("email") email: RequestBody,
        @Part("numTel") numTel: RequestBody?,
        @Part("dateNaissance") dateNaissance: RequestBody,
        @Part("genre") genre: RequestBody,
        @Part("password") password: RequestBody,
        @Part("bio") bio: RequestBody,
        @Part pdf: MultipartBody.Part
    ): Response<ArtistRequest>
}