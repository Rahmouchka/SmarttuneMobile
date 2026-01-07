package com.isetr.smarttune.data

import android.content.Context
import com.isetr.smarttune.data.dto.ChansonResponse

class ChansonRepository(context: Context) {

    private val api = RetrofitClient.getChansonApi()

    // Fetch songs from API based on search query
    suspend fun searchChansons(query: String): Result<List<ChansonResponse>> {
        return try {
            android.util.Log.d("ChansonRepository", "Searching chansons with query: '$query'")
            val response = api.searchChansons(query)
            android.util.Log.d("ChansonRepository", "Response code: ${response.code()}")
            android.util.Log.d("ChansonRepository", "Response successful: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val chansons = response.body()!!
                android.util.Log.d("ChansonRepository", "Got ${chansons.size} chansons")
                Result.success(chansons)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                android.util.Log.d("ChansonRepository", "Error body: $errorBody")
                val errorMsg = "Erreur serveur: ${response.code()} - ${response.message()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            android.util.Log.e("ChansonRepository", "Exception: ${e.message}", e)
            val errorMsg = when {
                e.message?.contains("Failed to connect") == true ->
                    "Impossible de se connecter au serveur.\n" +
                    "Vérifie que:\n" +
                    "1. Le serveur est démarré (port 8082)\n" +
                    "2. Ton WiFi est correct"
                e.message?.contains("timeout") == true ->
                    "Connexion au serveur trop lente"
                e.message?.contains("Unable to resolve host") == true ->
                    "Impossible de résoudre l'adresse serveur"
                else -> "Erreur réseau: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    // Fetch random songs by mood
    suspend fun getRandomChansons(humeur: String): Result<List<ChansonResponse>> {
        return try {
            val response = api.getRandomChansons(humeur)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Erreur serveur: ${response.code()} - ${response.message()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("Failed to connect") == true ->
                    "Impossible de se connecter au serveur.\n" +
                    "Vérifie que:\n" +
                    "1. Le serveur est démarré (port 8082)\n" +
                    "2. Ton WiFi est correct"
                e.message?.contains("timeout") == true ->
                    "Connexion au serveur trop lente"
                e.message?.contains("Unable to resolve host") == true ->
                    "Impossible de résoudre l'adresse serveur"
                else -> "Erreur réseau: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }
}

