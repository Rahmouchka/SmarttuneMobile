package com.isetr.smarttune.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isetr.smarttune.data.FavorisRepository
import com.isetr.smarttune.data.database.SmartTuneDatabase
import com.isetr.smarttune.data.dto.ChansonSimple
import kotlinx.coroutines.launch

sealed class FavorisUiState {
    object Idle : FavorisUiState()
    object Loading : FavorisUiState()
    data class Success(val favoris: List<ChansonSimple>) : FavorisUiState()
    data class Error(val message: String) : FavorisUiState()
}

class FavorisViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavorisRepository()
    private val sessionDao = SmartTuneDatabase.getDatabase(application).sessionDao()

    private val _uiState = MutableLiveData<FavorisUiState>(FavorisUiState.Idle)
    val uiState: LiveData<FavorisUiState> = _uiState

    private val _favorisIds = MutableLiveData<Set<Long>>(emptySet())
    val favorisIds: LiveData<Set<Long>> = _favorisIds

    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess

    private val _operationError = MutableLiveData<String?>()
    val operationError: LiveData<String?> = _operationError

    fun loadFavoris() {
        _uiState.value = FavorisUiState.Loading
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _uiState.value = FavorisUiState.Error("Utilisateur non connecté")
                return@launch
            }

            val result = repository.getFavoris(session.userId)
            result.fold(
                onSuccess = { favoris ->
                    _uiState.value = FavorisUiState.Success(favoris)
                    _favorisIds.value = favoris.map { it.id }.toSet()
                },
                onFailure = { error ->
                    _uiState.value = FavorisUiState.Error(error.message ?: "Erreur inconnue")
                }
            )
        }
    }

    fun isFavorite(chansonId: Long): Boolean {
        return _favorisIds.value?.contains(chansonId) ?: false
    }

    fun addToFavoris(chansonId: Long) {
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _operationError.value = "Utilisateur non connecté"
                return@launch
            }

            val result = repository.addToFavoris(session.userId, chansonId)
            result.fold(
                onSuccess = {
                    _operationSuccess.value = "Ajouté aux favoris"
                    val currentIds = _favorisIds.value?.toMutableSet() ?: mutableSetOf()
                    currentIds.add(chansonId)
                    _favorisIds.value = currentIds
                    loadFavoris()
                },
                onFailure = { error ->
                    _operationError.value = error.message
                }
            )
        }
    }

    fun removeFromFavoris(chansonId: Long) {
        viewModelScope.launch {
            val session = sessionDao.getCurrentSessionAsync()
            if (session == null) {
                _operationError.value = "Utilisateur non connecté"
                return@launch
            }

            val result = repository.removeFromFavoris(session.userId, chansonId)
            result.fold(
                onSuccess = {
                    _operationSuccess.value = "Retiré des favoris"
                    val currentIds = _favorisIds.value?.toMutableSet() ?: mutableSetOf()
                    currentIds.remove(chansonId)
                    _favorisIds.value = currentIds
                    loadFavoris()
                },
                onFailure = { error ->
                    _operationError.value = error.message
                }
            )
        }
    }

    fun toggleFavoris(chansonId: Long) {
        if (isFavorite(chansonId)) {
            removeFromFavoris(chansonId)
        } else {
            addToFavoris(chansonId)
        }
    }

    fun clearMessages() {
        _operationSuccess.value = null
        _operationError.value = null
    }
}

