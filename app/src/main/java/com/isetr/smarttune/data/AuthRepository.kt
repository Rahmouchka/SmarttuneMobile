package com.isetr.smarttune.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.isetr.smarttune.data.database.SessionEntity
import com.isetr.smarttune.data.database.SmartTuneDatabase
import com.isetr.smarttune.data.dto.ArtistRequest
import com.isetr.smarttune.data.dto.LoginRequest
import com.isetr.smarttune.data.dto.UserRegistrationRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AuthRepository(context: Context) {

    private val sessionDao = SmartTuneDatabase.getDatabase(context).sessionDao()
    private val api = RetrofitClient.getAuthApi()

    // Obtenir la session courante (pour vérifier si connecté)
    fun getCurrentSession(): LiveData<SessionEntity?> {
        return sessionDao.getCurrentSession()
    }

    // LOGIN VIA API - Authentifier auprès du serveur
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val request = LoginRequest(email = email, password = password)
            val response = api.login(request)

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!

                // Sauvegarder la session (userId + userRole + username + email)
                val session = SessionEntity(
                    userId = user.id,
                    username = user.username,
                    email = user.email,
                    userRole = user.role
                )
                sessionDao.saveSession(session)

                Result.success(user)
            } else {
                val errorMsg = "Erreur serveur: ${response.code()} - ${response.message()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            // Message d'erreur détaillé
            val errorMsg = when {
                e.message?.contains("Failed to connect") == true ->
                    "Impossible de se connecter au serveur.\n" +
                    "Vérifie que:\n" +
                    "1. Le serveur est démarré (port 8082)\n" +
                    "2. Ton téléphone est sur le même WiFi (192.168.1.x)\n" +
                    "3. L'adresse IP est correcte (192.168.1.12)"
                e.message?.contains("timeout") == true ->
                    "Connexion au serveur trop lente (timeout 30s)"
                e.message?.contains("Unable to resolve host") == true ->
                    "Impossible de résoudre l'adresse serveur"
                else -> "Erreur réseau: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    // REGISTER USER VIA API
    suspend fun registerUser(
        username: String,
        nom: String,
        prenom: String,
        email: String,
        numTel: String?,
        dateNaissance: String,  // Format: "YYYY-MM-DD"
        genre: String,  // "H" ou "F"
        password: String
    ): Result<User> {
        return try {
            val request = UserRegistrationRequest(
                username = username,
                nom = nom,
                prenom = prenom,
                email = email,
                numTel = if (numTel.isNullOrBlank()) null else numTel,
                dateNaissance = dateNaissance,  // Envoyé directement en tant que String
                genre = genre,
                password = password
            )
            val response = api.registerUser(request)

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!

                // Sauvegarder la session
                val session = SessionEntity(
                    userId = user.id,
                    username = user.username,
                    email = user.email,
                    userRole = user.role
                )
                sessionDao.saveSession(session)

                Result.success(user)
            } else {
                val errorMsg = "Erreur d'inscription (${response.code()}): ${response.message()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("Failed to connect") == true ->
                    "Impossible de se connecter au serveur.\nVérifie que:\n1. Le serveur est démarré\n2. L'adresse IP est correcte"
                e.message?.contains("timeout") == true ->
                    "Connexion au serveur trop lente"
                e.message?.contains("Unable to resolve host") == true ->
                    "Impossible de résoudre l'adresse serveur"
                else -> "Erreur d'inscription: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    // REGISTER ARTIST VIA API
    suspend fun registerArtist(
        username: String,
        nom: String,
        prenom: String,
        email: String,
        numTel: String?,
        dateNaissance: String,  // Format: "YYYY-MM-DD"
        genre: String,  // "H" ou "F"
        password: String,
        bio: String,
        pdfFile: File
    ): Result<ArtistRequest> {
        return try {
            val pdfPart = MultipartBody.Part.createFormData(
                "pdf",
                pdfFile.name,
                pdfFile.asRequestBody("application/pdf".toMediaTypeOrNull())
            )

            val response = api.registerArtist(
                username = username.toRequestBody("text/plain".toMediaTypeOrNull()),
                nom = nom.toRequestBody("text/plain".toMediaTypeOrNull()),
                prenom = prenom.toRequestBody("text/plain".toMediaTypeOrNull()),
                email = email.toRequestBody("text/plain".toMediaTypeOrNull()),
                numTel = if (numTel.isNullOrBlank()) null else numTel.toRequestBody("text/plain".toMediaTypeOrNull()),
                dateNaissance = dateNaissance.toRequestBody("text/plain".toMediaTypeOrNull()),  // String directement
                genre = genre.toRequestBody("text/plain".toMediaTypeOrNull()),
                password = password.toRequestBody("text/plain".toMediaTypeOrNull()),
                bio = bio.toRequestBody("text/plain".toMediaTypeOrNull()),
                pdf = pdfPart
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Erreur serveur (${response.code()})"
                Result.failure(Exception("Demande artiste refusée: $error"))
            }
        } catch (e: Exception) {
            val errorMsg = when {
                e.message?.contains("Failed to connect") == true ->
                    "Impossible de se connecter au serveur.\nVérifie que:\n1. Le serveur est démarré\n2. L'adresse IP est correcte"
                e.message?.contains("timeout") == true ->
                    "Connexion au serveur trop lente"
                e.message?.contains("Unable to resolve host") == true ->
                    "Impossible de résoudre l'adresse serveur"
                else -> "Erreur d'inscription artiste: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    // Déconnecter l'utilisateur
    suspend fun logout() {
        sessionDao.clearSession()
    }
}

