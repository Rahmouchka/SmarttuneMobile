package com.isetr.smarttune.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.isetr.smarttune.data.ChansonRepository
import com.isetr.smarttune.data.dto.ChansonResponse
import kotlinx.coroutines.launch

sealed class ChansonUiState {
    object Idle : ChansonUiState()
    object Loading : ChansonUiState()
    data class Success(val chansons: List<ChansonResponse>) : ChansonUiState()
    data class Error(val message: String) : ChansonUiState()
}

class ChansonViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChansonRepository(application.applicationContext)

    private val _uiState = MutableLiveData<ChansonUiState>(ChansonUiState.Idle)
    val uiState: LiveData<ChansonUiState> = _uiState

    // Rechercher des chansons via API
    fun searchChansons(query: String) {
        if (query.isBlank()) {
            _uiState.value = ChansonUiState.Idle
            return
        }

        _uiState.value = ChansonUiState.Loading
        viewModelScope.launch {
            val result = repository.searchChansons(query)
            result.fold(
                onSuccess = { chansons ->
                    _uiState.value = ChansonUiState.Success(chansons)
                },
                onFailure = { error ->
                    _uiState.value = ChansonUiState.Error(error.message ?: "Erreur inconnue")
                }
            )
        }
    }

    // Charger les chansons aléatoires au démarrage
    fun loadRandomChansons(humeur: String = "POP") {
        _uiState.value = ChansonUiState.Loading
        viewModelScope.launch {
            val result = repository.getRandomChansons(humeur)
            result.fold(
                onSuccess = { chansons ->
                    _uiState.value = ChansonUiState.Success(chansons)
                },
                onFailure = { error ->
                    _uiState.value = ChansonUiState.Error(error.message ?: "Erreur inconnue")
                }
            )
        }
    }
}

