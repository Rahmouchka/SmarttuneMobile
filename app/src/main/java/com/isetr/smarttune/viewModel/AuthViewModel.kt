// viewmodel/AuthViewModel.kt
package com.isetr.smarttune.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isetr.smarttune.data.dto.ArtistRequest
import com.isetr.smarttune.data.AuthRepository
import com.isetr.smarttune.data.User
import com.isetr.smarttune.data.database.SessionEntity
import kotlinx.coroutines.launch
import java.io.File

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class SuccessUser(val user: User) : AuthUiState()
    data class SuccessArtist(val artistRequest: ArtistRequest) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application.applicationContext)

    // UI State pour login/register
    private val _uiState = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val uiState: LiveData<AuthUiState> = _uiState

    // Session courante (pour navigation)
    val authenticatedUser: LiveData<SessionEntity?> = repository.getCurrentSession()

    // Login via API
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Tous les champs sont obligatoires")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { user: User -> _uiState.value = AuthUiState.SuccessUser(user) },
                onFailure = { error: Throwable -> _uiState.value = AuthUiState.Error(error.message ?: "Erreur de connexion") }
            )
        }
    }

    // Register user via API
    fun registerUser(
        username: String,
        nom: String,
        prenom: String,
        email: String,
        numTel: String?,
        dateNaissance: String,  // Format: "dd/MM/yyyy"
        genre: String,  // "H" ou "F"
        password: String
    ) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.registerUser(username, nom, prenom, email, numTel, dateNaissance, genre, password)
            result.fold(
                onSuccess = { user: User -> _uiState.value = AuthUiState.SuccessUser(user) },
                onFailure = { error: Throwable -> _uiState.value = AuthUiState.Error(error.message ?: "Inscription échouée") }
            )
        }
    }

    // Register artist via API
    fun registerArtist(
        username: String,
        nom: String,
        prenom: String,
        email: String,
        numTel: String?,
        dateNaissance: String,  // Format: "dd/MM/yyyy"
        genre: String,  // "H" ou "F"
        password: String,
        bio: String,
        pdfFile: File
    ) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.registerArtist(username, nom, prenom, email, numTel, dateNaissance, genre, password, bio, pdfFile)
            result.fold(
                onSuccess = { artist: ArtistRequest -> _uiState.value = AuthUiState.SuccessArtist(artist) },
                onFailure = { error: Throwable -> _uiState.value = AuthUiState.Error(error.message ?: "Demande artiste échouée") }
            )
        }
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}





