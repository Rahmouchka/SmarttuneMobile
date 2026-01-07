package com.isetr.smarttune.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isetr.smarttune.data.PlaylistRepository
import com.isetr.smarttune.data.database.SmartTuneDatabase
import com.isetr.smarttune.data.dto.PlaylistResponse
import kotlinx.coroutines.launch

sealed class PlaylistUiState {
    object Idle : PlaylistUiState()
    object Loading : PlaylistUiState()
    data class Success(val playlists: List<PlaylistResponse>) : PlaylistUiState()
    data class Error(val message: String) : PlaylistUiState()
}

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlaylistRepository()
    private val sessionDao = SmartTuneDatabase.getDatabase(application).sessionDao()

    private val _uiState = MutableLiveData<PlaylistUiState>(PlaylistUiState.Idle)
    val uiState: LiveData<PlaylistUiState> = _uiState

    private val _selectedPlaylist = MutableLiveData<PlaylistResponse?>()
    val selectedPlaylist: LiveData<PlaylistResponse?> = _selectedPlaylist

    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess

    private val _operationError = MutableLiveData<String?>()
    val operationError: LiveData<String?> = _operationError

    // Charger les playlists
    fun loadPlaylists() {
        _uiState.value = PlaylistUiState.Loading
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _uiState.value = PlaylistUiState.Error("Utilisateur non connecté")
                return@launch
            }

            val result = repository.getPlaylists(session.userId)
            result.fold(
                onSuccess = { playlists ->
                    _uiState.value = PlaylistUiState.Success(playlists)
                },
                onFailure = { error ->
                    _uiState.value = PlaylistUiState.Error(error.message ?: "Erreur inconnue")
                }
            )
        }
    }

    // Créer une playlist
    fun createPlaylist(titre: String, visible: Boolean = true) {
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _operationError.value = "Utilisateur non connecté"
                return@launch
            }

            val result = repository.createPlaylist(session.userId, titre, visible)
            result.fold(
                onSuccess = { playlist ->
                    _operationSuccess.value = "Playlist créée: ${playlist.titre}"
                    loadPlaylists() // Recharger la liste
                },
                onFailure = { error ->
                    _operationError.value = error.message
                }
            )
        }
    }

    // Mettre à jour une playlist
    fun updatePlaylist(playlistId: Long, titre: String, visible: Boolean) {
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _operationError.value = "Utilisateur non connecté"
                return@launch
            }

            val result = repository.updatePlaylist(session.userId, playlistId, titre, visible)
            result.fold(
                onSuccess = { playlist ->
                    _operationSuccess.value = "Playlist mise à jour"
                    _selectedPlaylist.value = playlist
                    loadPlaylists() // Recharger la liste
                },
                onFailure = { error ->
                    _operationError.value = error.message
                }
            )
        }
    }

    // Supprimer une playlist
    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _operationError.value = "Utilisateur non connecté"
                return@launch
            }

            val result = repository.deletePlaylist(session.userId, playlistId)
            result.fold(
                onSuccess = {
                    _operationSuccess.value = "Playlist supprimée"
                    _selectedPlaylist.value = null
                    loadPlaylists() // Recharger la liste
                },
                onFailure = { error ->
                    _operationError.value = error.message
                }
            )
        }
    }

    // Ajouter des chansons à une playlist
    fun addChansonsToPlaylist(playlistId: Long, chansonIds: List<Long>) {
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _operationError.value = "Utilisateur non connecté"
                return@launch
            }

            val result = repository.addChansonsToPlaylist(session.userId, playlistId, chansonIds)
            result.fold(
                onSuccess = { playlist ->
                    _operationSuccess.value = "Chanson(s) ajoutée(s) à la playlist"
                    _selectedPlaylist.value = playlist
                    loadPlaylists()
                },
                onFailure = { error ->
                    _operationError.value = error.message
                }
            )
        }
    }

    // Retirer une chanson d'une playlist
    fun removeChansonFromPlaylist(playlistId: Long, chansonId: Long) {
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _operationError.value = "Utilisateur non connecté"
                return@launch
            }

            val result = repository.removeChansonFromPlaylist(session.userId, playlistId, chansonId)
            result.fold(
                onSuccess = { playlist ->
                    _operationSuccess.value = "Chanson retirée de la playlist"
                    _selectedPlaylist.value = playlist
                    loadPlaylists()
                },
                onFailure = { error ->
                    _operationError.value = error.message
                }
            )
        }
    }

    // Sélectionner une playlist
    fun selectPlaylist(playlist: PlaylistResponse) {
        _selectedPlaylist.value = playlist
    }

    // Créer une playlist et y ajouter une chanson
    fun createPlaylistAndAddSong(titre: String, chansonId: Long) {
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _operationError.value = "Utilisateur non connecté"
                return@launch
            }

            // 1. Créer la playlist
            val createResult = repository.createPlaylist(session.userId, titre, true)
            createResult.fold(
                onSuccess = { playlist ->
                    // 2. Ajouter la chanson à la playlist créée
                    val addResult = repository.addChansonsToPlaylist(session.userId, playlist.id, listOf(chansonId))
                    addResult.fold(
                        onSuccess = {
                            _operationSuccess.value = "Chanson ajoutée à \"${playlist.titre}\""
                            loadPlaylists()
                        },
                        onFailure = { error ->
                            _operationError.value = "Playlist créée mais erreur: ${error.message}"
                        }
                    )
                },
                onFailure = { error ->
                    _operationError.value = error.message
                }
            )
        }
    }

    // Clear messages
    fun clearMessages() {
        _operationSuccess.value = null
        _operationError.value = null
    }
}

