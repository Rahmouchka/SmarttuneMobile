package com.isetr.smarttune.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val API_PORT = 8082
    private const val USE_EMULATOR = true  // ✅ Émulateur Android Studio

    // ✅ 10.0.2.2 = adresse spéciale pour localhost du PC dans l'émulateur
    private val BASE_URL = if (USE_EMULATOR) {
        "http://10.0.2.2:$API_PORT/"  // Émulateur Android
    } else {
        "http://192.168.1.12:$API_PORT/"  // PC réel (si appareil physique)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
                try {
                    // Essayer le format ISO (YYYY-MM-DD)
                    LocalDate.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    try {
                        // Essayer d'autres formats si nécessaire
                        LocalDate.parse(json.asString)
                    } catch (e2: Exception) {
                        null
                    }
                }
            })
            .create()
    }

    fun getClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun getRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }

    fun getAuthApi() =
        getRetrofitInstance().create(AuthApi::class.java)

    fun getChansonApi() =
        getRetrofitInstance().create(ChansonApi::class.java)

    fun getFavorisApi() =
        getRetrofitInstance().create(FavorisApi::class.java)

    fun getPlaylistApi() =
        getRetrofitInstance().create(PlaylistApi::class.java)
}